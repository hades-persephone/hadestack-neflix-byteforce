package io.watch.history.service;

import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.netty.handler.timeout.WriteTimeoutException;
import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;
import io.watch.history.exception.CassandraPersistenceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service dedicated to handling direct Cassandra persistence operations.
 * This service isolates database operations from business logic and failure handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CassandraPersistenceService {

    private final CassandraTemplate cassandraTemplate;
    private final CassandraOperations cassandraOperations;

    /**
     * Save a single action history record with retry mechanism
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public CompletableFuture<Void> saveActionHistory(ActionHistoryByAction activity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Attempting to save action history: {}", activity.getId());
                cassandraTemplate.insert(activity);
                log.debug("Successfully saved action history: {}", activity.getId());
                return null;
            } catch (Exception e) {
                log.error("Failed to save action history: {}, Error: {}",
                        activity.getId(), e.getMessage());
                throw new CassandraPersistenceException("Failed to save action history", e);
            }
        });
    }

    /**
     * Save user history record with retry mechanism
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public CompletableFuture<Void> saveUserHistory(ActionHistoryByUser userActivity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Attempting to save user history: {}", userActivity.getKey().getUserId());
                cassandraTemplate.insert(userActivity);
                log.debug("Successfully saved user history: {}", userActivity.getKey().getUserId());
                return null;
            } catch (Exception e) {
                log.error("Failed to save user history: {}, Error: {}",
                        userActivity.getKey().getUserId(), e.getMessage());
                throw new CassandraPersistenceException("Failed to save user history", e);
            }
        });
    }

    /**
     * Update watch progress with retry mechanism
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 1.5, random = true)
    )
    public CompletableFuture<Void> updateWatchProgress(WatchProgress progress) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Attempting to update watch progress: {}", progress.getProgressId());
                cassandraTemplate.insert(progress);
                log.debug("Successfully updated watch progress: {}", progress.getProgressId());
                return null;
            } catch (Exception e) {
                log.error("Failed to update watch progress: {}, Error: {}",
                        progress.getProgressId(), e.getMessage());
                throw new CassandraPersistenceException("Failed to update watch progress", e);
            }
        });
    }

    /**
     * Save batch of action histories using Cassandra batch operations
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5, random = true)
    )
    public CompletableFuture<Void> saveBatchHistory(List<ActionHistoryByAction> activities) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (activities.isEmpty()) {
                    return null;
                }

                log.debug("Attempting to save batch history: {} items", activities.size());

                // Use batch statement for better performance
                BatchStatementBuilder batchBuilder = BatchStatement.builder(DefaultBatchType.LOGGED);

                for (ActionHistoryByAction activity : activities) {
                    SimpleStatement statement = SimpleStatement.builder(
                                    "INSERT INTO action_history_by_action " +
                                            "(action_type, action_timestamp, user_id, entity_type, entity_id, details, source_ip, user_agent) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                            .addPositionalValues(
                                    activity.getKey().getActionType(),
                                    activity.getKey().getActionTimestamp(),
                                    activity.getKey().getUserId(),
                                    activity.getEntityType(),
                                    activity.getEntityId(),
                                    activity.getDetails(),
                                    activity.getSourceIp(),
                                    activity.getUserAgent()
                            )
                            .build();
                    batchBuilder.addStatement(statement);
                }

                cassandraOperations.getCqlOperations().execute(batchBuilder.build());
                log.debug("Successfully saved batch history: {} items", activities.size());
                return null;
            } catch (Exception e) {
                log.error("Failed to save batch history: {} items, Error: {}",
                        activities.size(), e.getMessage());
                throw new CassandraPersistenceException("Failed to save batch history", e);
            }
        });
    }

    /**
     * Save batch of user histories
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5, random = true)
    )
    public CompletableFuture<Void> saveBatchUserHistory(List<ActionHistoryByUser> userActivities) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (userActivities.isEmpty()) {
                    return null;
                }

                log.debug("Attempting to save batch user history: {} items", userActivities.size());

                // Use Spring Data Cassandra's batch save
                cassandraTemplate.batchOps().insert(userActivities).execute();

                log.debug("Successfully saved batch user history: {} items", userActivities.size());
                return null;
            } catch (Exception e) {
                log.error("Failed to save batch user history: {} items, Error: {}",
                        userActivities.size(), e.getMessage());
                throw new CassandraPersistenceException("Failed to save batch user history", e);
            }
        });
    }

    /**
     * Batch update watch progress
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 1.5, random = true)
    )
    public CompletableFuture<Void> updateBatchWatchProgress(List<WatchProgress> progressList) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (progressList.isEmpty()) {
                    return null;
                }

                log.debug("Attempting to update batch watch progress: {} items", progressList.size());

                cassandraTemplate.batchOps().insert(progressList).execute();

                log.debug("Successfully updated batch watch progress: {} items", progressList.size());
                return null;
            } catch (Exception e) {
                log.error("Failed to update batch watch progress: {} items, Error: {}",
                        progressList.size(), e.getMessage());
                throw new CassandraPersistenceException("Failed to update batch watch progress", e);
            }
        });
    }

    /**
     * Perform a health check on Cassandra connection
     */
    public CompletableFuture<Boolean> healthCheck() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Performing Cassandra health check");

                // Simple query to test connectivity
                cassandraOperations.getCqlOperations().queryForObject(
                        "SELECT now() FROM system.local", String.class);

                log.debug("Cassandra health check passed");
                return true;
            } catch (Exception e) {
                log.warn("Cassandra health check failed: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Get connection statistics for monitoring
     */
    public CompletableFuture<CassandraConnectionStats> getConnectionStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // This would typically query system tables for connection info
                return CassandraConnectionStats.builder()
                        .isConnected(true)
                        .lastSuccessfulOperation(Instant.now())
                        .build();
            } catch (Exception e) {
                log.error("Failed to get connection stats", e);
                return CassandraConnectionStats.builder()
                        .isConnected(false)
                        .lastError(e.getMessage())
                        .build();
            }
        });
    }

    /**
     * Execute a raw CQL query with timeout
     */
    public CompletableFuture<Object> executeWithTimeout(String cql, Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Executing CQL with timeout: {}", cql);
                cassandraOperations.getCqlOperations().execute(cql);
                return null;
            } catch (Exception e) {
                log.error("Failed to execute CQL: {}", cql, e);
                throw new CassandraPersistenceException("Failed to execute CQL", e);
            }
        }).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // Recovery methods for retry failures
    @Recover
    public CompletableFuture<Void> recoverSaveActionHistory(Exception ex, ActionHistoryByAction activity) {
        log.error("All retry attempts failed for action history: {}", activity.getId(), ex);
        return CompletableFuture.failedFuture(
                new CassandraPersistenceException("Failed to save action history after retries", ex));
    }

    @Recover
    public CompletableFuture<Void> recoverSaveUserHistory(Exception ex, ActionHistoryByUser userActivity) {
        log.error("All retry attempts failed for user history: {}", userActivity.getKey().getUserId(), ex);
        return CompletableFuture.failedFuture(
                new CassandraPersistenceException("Failed to save user history after retries", ex));
    }

    @Recover
    public CompletableFuture<Void> recoverUpdateWatchProgress(Exception ex, WatchProgress progress) {
        log.error("All retry attempts failed for watch progress: {}", progress.getProgressId(), ex);
        return CompletableFuture.failedFuture(
                new CassandraPersistenceException("Failed to update watch progress after retries", ex));
    }

    @Recover
    public CompletableFuture<Void> recoverSaveBatchHistory(Exception ex, List<ActionHistoryByAction> activities) {
        log.error("All retry attempts failed for batch history: {} items", activities.size(), ex);
        return CompletableFuture.failedFuture(
                new CassandraPersistenceException("Failed to save batch history after retries", ex));
    }

    @Recover
    public CompletableFuture<Void> recoverSaveBatchUserHistory(Exception ex, List<ActionHistoryByUser> userActivities) {
        log.error("All retry attempts failed for batch user history: {} items", userActivities.size(), ex);
        return CompletableFuture.failedFuture(
                new CassandraPersistenceException("Failed to save batch user history after retries", ex));
    }

    @Recover
    public CompletableFuture<Void> recoverUpdateBatchWatchProgress(Exception ex, List<WatchProgress> progressList) {
        log.error("All retry attempts failed for batch watch progress: {} items", progressList.size(), ex);
        return CompletableFuture.failedFuture(
                new CassandraPersistenceException("Failed to update batch watch progress after retries", ex));
    }

    /**
     * Connection statistics model
     */
    @Getter
    public static class CassandraConnectionStats {
        private final boolean isConnected;
        private final Instant lastSuccessfulOperation;
        private final String lastError;

        private CassandraConnectionStats(boolean isConnected, Instant lastSuccessfulOperation, String lastError) {
            this.isConnected = isConnected;
            this.lastSuccessfulOperation = lastSuccessfulOperation;
            this.lastError = lastError;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean isConnected;
            private Instant lastSuccessfulOperation;
            private String lastError;

            public Builder isConnected(boolean isConnected) {
                this.isConnected = isConnected;
                return this;
            }

            public Builder lastSuccessfulOperation(Instant lastSuccessfulOperation) {
                this.lastSuccessfulOperation = lastSuccessfulOperation;
                return this;
            }

            public Builder lastError(String lastError) {
                this.lastError = lastError;
                return this;
            }

            public CassandraConnectionStats build() {
                return new CassandraConnectionStats(isConnected, lastSuccessfulOperation, lastError);
            }
        }
    }
}