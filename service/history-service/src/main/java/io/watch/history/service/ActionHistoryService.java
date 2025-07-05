package io.watch.history.service;

import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.timeout.WriteTimeoutException;
import io.watch.history.dto.ActionRecord;
import io.watch.history.entity.*;
import io.watch.history.handler.UserActivityFailureHandler;
import io.watch.history.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionHistoryService {

    private final ActionHistoryByEntityRepository byEntityRepository;
    private final ActionHistoryByUserRepository byUserRepository;
    private final ActionHistoryByActionRepository byActionRepository;
    private final ActionHistoryByTimeRepository byTimeRepository;
    private final ActionHistoryStatsRepository statsRepository;
    private final UserActivityFailureHandler failureHandler;
    private final CassandraOperations cassandraOperations;
    private final CassandraTemplate cassandraTemplate;
    private final RetryTemplate retryTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, ActionRecord> kafkaTemplate;
    private final CassandraPersistenceService cassandraPersistenceService;


    @Value("${action-history.async-processing:true}")
    private boolean asyncProcessing;

    @Value("${action-history.batch-size:100}")
    private int batchSize;

    /**
     * Record a single action
     *
     * @param actionRecord The action record to save
     * @return CompletableFuture that completes when the record is saved
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public CompletableFuture<Void> recordAction(ActionRecord actionRecord) {
        if (asyncProcessing) {
            return saveActionAsync(actionRecord);
        } else {
            saveAction(actionRecord);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Record multiple actions in batch
     *
     * @param actionRecords List of action records to save
     * @return CompletableFuture that completes when all records are saved
     */
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public CompletableFuture<Void> recordActions(List<ActionRecord> actionRecords) {
        if (asyncProcessing) {
            return saveActionsAsync(actionRecords);
        } else {
            saveActions(actionRecords);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Async
    public CompletableFuture<Void> saveUserActivityWithRetry(ActionHistoryByAction activity) {
        return cassandraPersistenceService.saveActionHistory(activity);
    }

    @Recover
    public CompletableFuture<Void> recoverSaveUserActivity(WriteTimeoutException ex, ActionHistoryByAction activity) {
        log.error("All retry attempts failed for user activity: {}, sending to DLQ",
                activity.getId(), ex);

        String operationId = UUID.randomUUID().toString();
        // Send to Dead Letter Queue hoặc alternative storage
        failureHandler.handleFailedWrite(activity, null, ex, operationId);
        return CompletableFuture.completedFuture(null);
    }

    // Manual retry với RetryTemplate
    public void saveUserActivityManualRetry(ActionHistoryByAction activity) {
        String operationId = UUID.randomUUID().toString();
        try {
            retryTemplate.execute(context -> {
                log.debug("Retry attempt {} for activity: {}",
                        context.getRetryCount() + 1, activity.getId());
                cassandraTemplate.insert(activity);
                return null;
            }, context -> {
                // Recovery callback
                log.error("Manual retry failed after {} attempts for activity: {}",
                        context.getRetryCount(), activity.getId());
                failureHandler.handleFailedWrite(activity, null, (Exception) context.getLastThrowable(), operationId);
                return null;
            });
        } catch (Exception e) {
            log.error("Unexpected error in manual retry", e);
        }
    }

    // Batch operations với retry
    @Retryable(
            retryFor = {WriteTimeoutException.class, NoNodeAvailableException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public void saveBatchUserActivities(List<ActionHistoryByAction> activities) {
        BatchStatementBuilder batchBuilder = BatchStatement.builder(DefaultBatchType.LOGGED);

        for (ActionHistoryByAction activity : activities) {
            SimpleStatement statement = SimpleStatement.builder(
                            "INSERT INTO user_activities (user_id, activity_date, timestamp, activity_type, item_id, metadata) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)"
                    )
                    .addPositionalValues(
                            activity.getKey().getUserId(),
                            activity.getKey().getActionTimestamp()
                    )
                    .build();

            batchBuilder.addStatement(statement);
        }

        cassandraTemplate.getCqlOperations().execute(batchBuilder.build());
        log.info("Successfully saved batch of {} activities", activities.size());
    }

    @Recover
    public void recoverSaveBatchUserActivities(Exception ex, List<ActionHistoryByAction> activities) {
        log.error("Batch retry failed for {} activities", activities.size(), ex);

        String operationId = UUID.randomUUID().toString();
        // Try individual saves as fallback
        activities.forEach(activity -> {
            try {
                saveUserActivityWithRetry(activity);
            } catch (Exception e) {
                failureHandler.handleFailedWrite(activity, null, e ,operationId);
            }
        });
    }

    /**
     * Get action history for an entity
     *
     * @param entityType Type of the entity
     * @param entityId   ID of the entity
     * @param limit      Maximum number of records to return
     * @return List of action records
     */
    public List<ActionRecord> getActionHistoryForEntity(
            String entityType, String entityId, int limit) {

        Slice<ActionHistoryByEntity> results = byEntityRepository.findByEntityTypeAndEntityId(
                entityType, entityId, PageRequest.of(0, limit));

        return results.getContent().stream()
                .map(this::mapToActionRecord)
                .collect(Collectors.toList());
    }

    /**
     * Get action history for a user
     *
     * @param userId ID of the user
     * @param limit  Maximum number of records to return
     * @return List of action records
     */
    public List<ActionRecord> getActionHistoryForUser(UUID userId, int limit) {
        String cacheKey = "user_history_" + userId + ":limit" + limit;
        List<ActionRecord> cached = Objects.requireNonNull(redisTemplate.opsForList().range(cacheKey, 0, limit - 1))
                .stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ActionRecord.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Lỗi parse JSON", e);
                    }
                })
                .toList();

        if(cached.isEmpty()) {
            log.debug("Cache hit for userId: {}, limit: {}", userId, limit);
            return cached;
        }
        Slice<ActionHistoryByUser> results = byUserRepository.findByKeyUserId(
                userId, PageRequest.of(0, limit));
        List<ActionRecord> records = results.getContent().stream()
                .map(this::mapToActionRecord)
                .toList();
        redisTemplate.opsForList().rightPushAll(cacheKey, records.stream()
                .map(record -> {
                    try {
                        return objectMapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList());
        redisTemplate.expire(cacheKey, Duration.ofMinutes(30));
        return records;
    }

    /**
     * Get action history for a specific action type
     *
     * @param actionType Type of action
     * @param limit      Maximum number of records to return
     * @return List of action records
     */
    public List<ActionRecord> getActionHistoryByType(String actionType, int limit) {
        Slice<ActionHistoryByAction> results = byActionRepository.findByKeyActionType(
                actionType, PageRequest.of(0, limit));

        return results.getContent().stream()
                .map(this::mapToActionRecord)
                .collect(Collectors.toList());
    }

    /**
     * Get action history for a specific time period
     *
     * @param yearMonth Year and month in format YYYY-MM
     * @param limit     Maximum number of records to return
     * @return List of action records
     */
    public List<ActionRecord> getActionHistoryByTime(String yearMonth, int limit) {
        Slice<ActionHistoryByTime> results = byTimeRepository.findByYearMonth(
                yearMonth, PageRequest.of(0, limit));

        return results.getContent().stream()
                .map(this::mapToActionRecord)
                .collect(Collectors.toList());
    }

    /**
     * Get action counts by entity type and action type
     *
     * @param entityType Type of the entity
     * @param actionType Type of action
     * @return Map of year-month to count
     */
    public Map<String, Long> getActionCounts(String entityType, String actionType) {
        List<ActionHistoryStats> stats = statsRepository.findByEntityTypeAndActionType(
                entityType, actionType);

        return stats.stream().collect(
                Collectors.toMap(ActionHistoryStats::getYearMonth, ActionHistoryStats::getCount));
    }

    /**
     * Save an action record synchronously
     */
    private void saveAction(ActionRecord actionRecord) {
        // If action timestamp is not set, use current time
        if (actionRecord.getActionTimestamp() == null) {
            actionRecord.setActionTimestamp(Instant.now());
        }

        // Save to all tables
        byEntityRepository.save(ActionHistoryByEntity.fromActionRecord(actionRecord));
        byUserRepository.save(ActionHistoryByUser.fromActionRecord(actionRecord));
        byActionRepository.save(ActionHistoryByAction.fromActionRecord(actionRecord));
        byTimeRepository.save(ActionHistoryByTime.fromActionRecord(actionRecord));

        // Update stats counter
        statsRepository.incrementCount(
                actionRecord.getEntityType(),
                actionRecord.getActionType(),
                actionRecord.getYearMonth());
        kafkaTemplate.send("user-action", String.valueOf(actionRecord.getKey().getUserId()), actionRecord)
                        .whenComplete((result, ex) -> {
                            if(ex != null) {
                                log.error("Failed to publish action to Kafka: {}", actionRecord.getUserId(), ex);
                                failureHandler.handleFailedKafkaPublish(actionRecord, ex);
                            } else {
                                log.debug("Successfully published action to Kafka: {}", actionRecord.getUserId());
                            }
                        });
        log.debug("Saved action record: {}", actionRecord);
    }

    /**
     * Save an action record asynchronously
     */
    @Async
    protected CompletableFuture<Void> saveActionAsync(ActionRecord actionRecord) {
        try {
            saveAction(actionRecord);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error saving action record asynchronously", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Save action records in batch
     */
    private void saveActions(List<ActionRecord> actionRecords) {
        if (actionRecords == null || actionRecords.isEmpty()) {
            return;
        }

        // Set timestamp if not present
        actionRecords.forEach(record -> {
            if (record.getActionTimestamp() == null) {
                record.setActionTimestamp(Instant.now());
            }
        });

        // Process in batches to avoid overwhelming Cassandra
        List<List<ActionRecord>> batches = new ArrayList<>();
        for (int i = 0; i < actionRecords.size(); i += batchSize) {
            batches.add(actionRecords.subList(i, Math.min(i + batchSize, actionRecords.size())));
        }

        for (List<ActionRecord> batch : batches) {
            // Convert to entity-specific records
            List<ActionHistoryByEntity> byEntityRecords = batch.stream()
                    .map(ActionHistoryByEntity::fromActionRecord)
                    .collect(Collectors.toList());

            List<ActionHistoryByUser> byUserRecords = batch.stream()
                    .map(ActionHistoryByUser::fromActionRecord)
                    .collect(Collectors.toList());

            List<ActionHistoryByAction> byActionRecords = batch.stream()
                    .map(ActionHistoryByAction::fromActionRecord)
                    .collect(Collectors.toList());

            List<ActionHistoryByTime> byTimeRecords = batch.stream()
                    .map(ActionHistoryByTime::fromActionRecord)
                    .collect(Collectors.toList());

            // Batch save
            byEntityRepository.saveAll(byEntityRecords);
            byUserRepository.saveAll(byUserRecords);
            byActionRepository.saveAll(byActionRecords);
            byTimeRepository.saveAll(byTimeRecords);

            // Update stats counters
            batch.forEach(record ->
                    statsRepository.incrementCount(
                            record.getEntityType(),
                            record.getActionType(),
                            record.getYearMonth())
            );
        }

        log.debug("Saved {} action records in batches", actionRecords.size());
    }

    /**
     * Save action records asynchronously in batch
     */
    @Async
    protected CompletableFuture<Void> saveActionsAsync(List<ActionRecord> actionRecords) {
        try {
            saveActions(actionRecords);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error saving action records asynchronously", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Map entity-specific records to the generic ActionRecord model
     */
    private ActionRecord mapToActionRecord(ActionHistoryByEntity record) {
        return ActionRecord.builder()
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .actionTimestamp(record.getActionTimestamp())
                .actionType(record.getActionType())
                .userId(record.getUserId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }

    private ActionRecord mapToActionRecord(ActionHistoryByUser record) {
        return ActionRecord.builder()
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .actionTimestamp(record.getKey().getActionTimestamp())
                .actionType(record.getKey().getActionType())
                .userId(record.getKey().getUserId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }

    private ActionRecord mapToActionRecord(ActionHistoryByAction record) {
        return ActionRecord.builder()
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .actionTimestamp(record.getKey().getActionTimestamp())
                .actionType(record.getKey().getActionType())
                .userId(record.getKey().getUserId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }

    private ActionRecord mapToActionRecord(ActionHistoryByTime record) {
        return ActionRecord.builder()
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .actionTimestamp(record.getActionTimestamp())
                .actionType(record.getActionType())
                .userId(record.getUserId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }

    public CompletableFuture<Boolean> healthCheck() {
        return cassandraPersistenceService.healthCheck();
    }

    public CompletableFuture<Void> saveUserHistory(ActionHistoryByUser userActivity) {
        return cassandraPersistenceService.saveUserHistory(userActivity);
    }

    public CompletableFuture<Void> saveBatchHistory(List<ActionHistoryByAction> activities) {
        return cassandraPersistenceService.saveBatchHistory(activities);
    }

    public CompletableFuture<Void> updateWatchProgress(WatchProgress progress) {
        return cassandraPersistenceService.updateWatchProgress(progress);
    }
}
