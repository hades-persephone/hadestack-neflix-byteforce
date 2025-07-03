package io.watch.history.handler;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.watch.history.config.ResilienceProperties;
import io.watch.history.dto.ActionRecord;
import io.watch.history.dto.FailedActivityRecord;
import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;
import io.watch.history.service.ActionHistoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActivityFailureHandler implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, FailedActivityRecord> kafkaTemplate;
    private final CircuitBreaker cassandraCircuitBreaker;
    private final Retry cassandraRetry;
    private final Bulkhead cassandraBulkhead;
    private final TimeLimiter timeLimiter;
    private final Executor asyncExecutor;
    private final ResilienceProperties resilienceProperties;
    private final MeterRegistry meterRegistry;

    private final ActionHistoryService actionHistoryService;
    private final List<FallbackStorageStrategy> fallbackStrategies;
    private final WatchHistoryMetrics metrics;


    private static final String FAILED_ACTIVITIES_KEY_PREFIX = "failed_activities:";
    private static final String CIRCUIT_BREAKER_METRICS_PREFIX = "cassandra.circuit.breaker";
    private static final String DLQ_TOPIC = "user-activity-dlq";
    private static final String RETRY_TOPIC = "user-activity-retry";

    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final Map<String, AtomicInteger> operationCounters = new ConcurrentHashMap<>();

    private volatile Instant lastHealthCheck = Instant.now();
    private volatile boolean isHealthy = true;

    @Async("asyncExecutor")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CompletableFuture<Void> saveActionHistoryWithResilience(ActionHistoryByAction actionHistoryByAction) {
        String operationId = UUID.randomUUID().toString();
        Timer.Sample timerSample = Timer.start(meterRegistry);
        return CompletableFuture.supplyAsync(() -> {
            activeRequests.incrementAndGet();
            incrementOperationCounter("save_action_history_attempts");
            try {
                log.debug("Starting saveActionHistory operation: {} for user: {}",
                        operationId, actionHistoryByAction.getUserId());
                Supplier<CompletableFuture<Void>> decoratedSupplier = decorateWithAllPatterns(() -> actionHistoryService.saveUserActivityWithRetry(actionHistoryByAction), "saveActionHistory");

                decoratedSupplier.get().get(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);

                metrics.incrementSuccessfulSaves();
                incrementOperationCounter("save_action_history_success");
                log.debug("Successfully saved action history: {}", operationId);
                return null;
            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker OPEN - using fallback storage strategy for action: {}", actionHistoryByAction);
                metrics.incrementCircuitBreakerFallbacks();
                incrementOperationCounter("save_action_history_circuit_breaker");
                return handleFallbackStorage(actionHistoryByAction, operationId);
            } catch (Exception e) {
                log.error("Faile to save action history: {}", actionHistoryByAction, e);
                metrics.incrementFailedSaves();
                incrementOperationCounter("save_action_history_failure");
                return handleFallbackStorage(actionHistoryByAction, operationId);
            } finally {
                activeRequests.decrementAndGet();
                timerSample.stop(Timer.builder("user_activity_save_duration")
                        .tag("operation", "save_action_history")
                        .register(meterRegistry));
            }
        }, asyncExecutor);
    }

    @Async("asyncExecutor")
    @Transactional
    public CompletableFuture<Void> saveUserHistoryWithResilience(@Valid @NotNull ActionHistoryByUser userActivity) {
        String operationId = UUID.randomUUID().toString();
        Timer.Sample timerSample = Timer.start(meterRegistry);

        return CompletableFuture.supplyAsync(() -> {
            activeRequests.incrementAndGet();
            incrementOperationCounter("save_user_history_attempts");
            try {
                log.debug("Starting saveUserHistory operation: {} for user: {}",
                        operationId, userActivity.getUserId());

                Supplier<CompletableFuture<Void>> decoratedSupplier = decorateWithAllPatterns(
                        () -> actionHistoryService.saveUserHistory(userActivity),
                        "saveUserHistory"
                );

                decoratedSupplier.get().get(Duration.ofSeconds(5).toMillis(),
                        java.util.concurrent.TimeUnit.MILLISECONDS);

                metrics.incrementSuccessfulSaves();
                incrementOperationCounter("save_user_history_success");
                log.debug("Successfully saved user history: {}", operationId);
                return null;

            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker OPEN - using fallback storage for user: {}", userActivity.getUserId());
                metrics.incrementCircuitBreakerFallbacks();
                incrementOperationCounter("save_user_history_circuit_breaker");
                return handleFallbackStorageHistoryByUser(userActivity, operationId);

            } catch (Exception e) {
                log.error("Failed to save user history: {}", userActivity.getUserId(), e);
                metrics.incrementFailedSaves();
                incrementOperationCounter("save_user_history_failure");
                return handleFallbackStorageHistoryByUser(userActivity, operationId);
            } finally {
                activeRequests.decrementAndGet();
                timerSample.stop(Timer.builder("user_activity_save_duration")
                        .tag("operation", "save_user_history")
                        .register(meterRegistry));
            }
        }, asyncExecutor);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Void> updateWatchProgressWithResilience(WatchProgress progress) {
        String operationId = UUID.randomUUID().toString();
        Timer.Sample timerSample = Timer.start(meterRegistry);
        return CompletableFuture.supplyAsync(() -> {
            activeRequests.incrementAndGet();
            incrementOperationCounter("update_progress_attempts");
            try {
                // Real-time progress updates are critical, use shorter timeout
                Supplier<CompletableFuture<Void>> decoratedSupplier =
                        decorateWithAllPatterns(() -> actionHistoryService.updateWatchProgress(progress),
                                "updateWatchProgress");

                decoratedSupplier.get().get(Duration.ofSeconds(2).toMillis(),
                        java.util.concurrent.TimeUnit.MILLISECONDS);

                metrics.incrementSuccessfulProgressUpdates();
                incrementOperationCounter("update_progress_success");
                log.debug("Updated watch progress: {}", operationId);
                return null;

            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker OPEN - caching progress update: {} operation: {}",
                        progress.getId(), operationId);
                metrics.incrementProgressUpdateFallbacks();
                incrementOperationCounter("update_progress_circuit_breaker");
                return handleProgressFallback(progress, operationId);

            } catch (Exception e) {
                log.error("Failed to update watch progress: {} operation: {}",
                        progress.getId(), operationId, e);
                metrics.incrementFailedProgressUpdates();
                incrementOperationCounter("update_progress_failure");
                return handleProgressFallback(progress, operationId);

            } finally {
                activeRequests.decrementAndGet();
                timerSample.stop(Timer.builder("user_activity_save_duration")
                        .tag("operation", "update_progress")
                        .register(meterRegistry));
            }
        }, asyncExecutor);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Void> saveBatchHistoryWithResilience(List<ActionHistoryByAction> activities) {
        if (activities.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        String operationId = UUID.randomUUID().toString();
        Timer.Sample timerSample = Timer.start(meterRegistry);

        return CompletableFuture.supplyAsync(() -> {
            activeRequests.incrementAndGet();
            incrementOperationCounter("save_batch_attempts");
            try {
                log.debug("Starting saveBatchHistory operation: {} with {} activities",
                        operationId, activities.size());

                // Validate batch size
                if (activities.size() > resilienceProperties.getMaxBatchSize()) {
                    log.warn("Batch size {} exceeds limit {}, splitting batch",
                            activities.size(), resilienceProperties.getMaxBatchSize());
                    return handleLargeBatch(activities, operationId);
                }
                Supplier<CompletableFuture<Void>> decoratedSupplier =
                        decorateWithAllPatterns(() -> actionHistoryService.saveBatchHistory(activities),
                                "saveBatchHistory");

                decoratedSupplier.get().get(Duration.ofSeconds(10).toMillis(),
                        java.util.concurrent.TimeUnit.MILLISECONDS);

                metrics.incrementSuccessfulBatchSaves();
                incrementOperationCounter("save_batch_success");
                log.info("Successfully saved batch: {} with {} activities", operationId, activities.size());
                return null;

            } catch (CallNotPermittedException e) {
                log.warn("Circuit breaker OPEN - storing batch in fallback: {} items operation: {}",
                        activities.size(), operationId);
                metrics.incrementBatchFallbacks();
                incrementOperationCounter("save_batch_circuit_breaker");
                return handleBatchFallback(activities, operationId);

            } catch (Exception e) {
                log.error("Failed to save batch history: {} items operation: {}",
                        activities.size(), operationId, e);
                metrics.incrementFailedBatchSaves();
                incrementOperationCounter("save_batch_failure");
                return handleBatchFallback(activities, operationId);

            } finally {
                activeRequests.decrementAndGet();
                timerSample.stop(Timer.builder("user_activity_save_duration")
                        .tag("operation", "save_batch")
                        .register(meterRegistry));
            }
        }, asyncExecutor);
    }

    private Void handleLargeBatch(List<ActionHistoryByAction> activities, String operationId) {
        int batchSize = resilienceProperties.getMaxBatchSize();
        List<List<ActionHistoryByAction>> chunks = activities.stream()
                .collect(Collectors.groupingBy(it -> activities.indexOf(it) / batchSize))
                .values()
                .stream()
                .toList();

        log.info("Splitting large batch {} into {} chunks", operationId, chunks.size());

        List<CompletableFuture<Void>> futures = chunks.stream()
                .map(this::saveBatchHistoryWithResilience)
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(resilienceProperties.getBatchTimeout().toMillis(), TimeUnit.MILLISECONDS);
            return null;
        } catch (Exception e) {
            log.error("Failed to save chunked batch: {}", operationId, e);
            return handleBatchFallback(activities, operationId);
        }
    }

    private Void handleFallbackStorage(ActionHistoryByAction activity, String operationId) {
        for (int i = 0; i < fallbackStrategies.size(); i++) {
            FallbackStorageStrategy strategy = fallbackStrategies.get(i);
            try {
                strategy.storeActionHistory(activity);
                log.info("Stored in fallback using strategy: {} for operation: {}",
                        strategy.getClass().getSimpleName(), operationId);
                metrics.incrementFallbackSuccess(strategy.getClass().getSimpleName());
                return null;
            } catch (Exception e) {
                log.warn("Fallback strategy {} failed for operation: {}",
                        strategy.getClass().getSimpleName(), operationId, e);
                metrics.incrementFallbackFailure(strategy.getClass().getSimpleName());

                // Try next strategy
                if (i == fallbackStrategies.size() - 1) {
                    // Last strategy failed, send to DLQ
                    handleFailedWrite(activity, null, e, operationId);
                }
            }
        }
        return null;
    }

    private Void handleFallbackStorageHistoryByUser(ActionHistoryByUser user, String operationId) {
        for (int i = 0; i < fallbackStrategies.size(); i++) {
            FallbackStorageStrategy strategy = fallbackStrategies.get(i);
            try {
                strategy.storeUserHistory(user);
                log.info("Stored in fallback using strategy: {} for operation: {}",
                        strategy.getClass().getSimpleName(), operationId);
                metrics.incrementFallbackSuccess(strategy.getClass().getSimpleName());
                return null;
            } catch (Exception e) {
                log.warn("Fallback strategy {} failed for operation: {}",
                        strategy.getClass().getSimpleName(), operationId, e);
                metrics.incrementFallbackFailure(strategy.getClass().getSimpleName());

                // Try next strategy
                if (i == fallbackStrategies.size() - 1) {
                    // Last strategy failed, send to DLQ
                    handleFailedWrite(null, user, e, operationId);
                }
            }
        }
        return null;
    }

    private Void handleFallbackStorage(ActionHistoryByUser userActivity) {
        for (FallbackStorageStrategy strategy : fallbackStrategies) {
            try {
                strategy.storeUserHistory(userActivity);
                log.info("Stored user history in fallback using: {}", strategy.getClass().getSimpleName());
                return null;
            } catch (Exception e) {
                log.warn("Fallback strategy {} failed", strategy.getClass().getSimpleName(), e);
            }
        }
        log.error("All fallback strategies failed for user: {}", userActivity.getUserId());
        return null;
    }

    private Void handleProgressFallback(WatchProgress progress, String operationId) {
        try {
            // For progress updates, use the fastest fallback strategy
            FallbackStorageStrategy fastestStrategy = fallbackStrategies.stream()
                    .min(Comparator.comparingInt(FallbackStorageStrategy::getPriority))
                    .orElse(fallbackStrategies.get(0));

            fastestStrategy.storeWatchProgress(progress);
            log.info("Stored progress in fallback using: {} for operation: {}",
                    fastestStrategy.getClass().getSimpleName(), operationId);
            metrics.incrementFallbackSuccess(fastestStrategy.getClass().getSimpleName());

            return null;
        } catch (Exception e) {
            log.error("Failed to store progress in fallback for operation: {}", operationId, e);
            handleFailedProgressWrite(progress, e, operationId);
            return null;
        }
    }

    private void handleFailedProgressWrite(WatchProgress progress, Exception exception, String operationId) {
        try {
            FailedActivityRecord failedRecord = FailedActivityRecord.builder()
                    .progress(progress)
                    .exception(exception.getMessage())
                    .timestamp(Instant.now())
                    .operationId(operationId)
                    .retryCount(0)
                    .build();

            kafkaTemplate.send(DLQ_TOPIC, String.valueOf(progress.getUserId()), failedRecord);
            log.info("Sent failed progress to DLQ: {} operation: {}", progress.getId(), operationId);

        } catch (Exception e) {
            log.error("Failed to handle progress write failure: {} operation: {}",
                    progress.getId(), operationId, e);
            logFailedProgress(progress, exception, operationId);
        }
    }

    private void logFailedUserActivity(ActionHistoryByUser userActivity, Exception exception, String operationId) {
        log.error("FAILED_USER_ACTIVITY|{}|{}|{}",
                userActivity.getUserId(),
                operationId,
                exception.getMessage());
    }

    private void logFailedProgress(WatchProgress progress, Exception exception, String operationId) {
        log.error("FAILED_PROGRESS|{}|{}|{}|{}",
                progress.getUserId(),
                progress.getId(),
                operationId,
                exception.getMessage());
    }

    private Void handleBatchFallback(List<ActionHistoryByAction> activities, String operationId) {
        try {
            // For batch operations, prefer strategies that support bulk operations
            FallbackStorageStrategy batchStrategy = fallbackStrategies.stream()
                    .filter(FallbackStorageStrategy::supportsBulkOperations)
                    .findFirst()
                    .orElse(fallbackStrategies.get(0));

            batchStrategy.storeBatchHistory(activities);
            log.info("Stored batch in fallback using: {} for operation: {}",
                    batchStrategy.getClass().getSimpleName(), operationId);
            metrics.incrementFallbackSuccess(batchStrategy.getClass().getSimpleName());

            return null;
        } catch (Exception e) {
            log.error("Failed to store batch in fallback for operation: {}", operationId, e);
            // Fallback to individual handling
            activities.forEach(activity -> handleFallbackStorage(activity, operationId));
            return null;
        }
    }
    public boolean isHealthy() {
        return cassandraCircuitBreaker.getState() == CircuitBreaker.State.CLOSED;
    }

    public CircuitBreaker.State getCircuitBreakerState() {
        return cassandraCircuitBreaker.getState();
    }

    @Async
    public void triggerRecoveryCheck() {
        if (cassandraCircuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN) {
            try {
                // Test with a simple health check query
                actionHistoryService.healthCheck().get(Duration.ofSeconds(2).toMillis(),
                        java.util.concurrent.TimeUnit.MILLISECONDS);
                log.info("Cassandra health check passed, circuit breaker should close");
            } catch (Exception e) {
                log.warn("Cassandra health check failed, circuit breaker remains open", e);
            }
        }
    }

    private <T> Supplier<CompletableFuture<T>> decorateWithAllPatterns(Supplier<CompletableFuture<T>> supplier, String operationName) {
        Supplier<CompletableFuture<T>> decoratedSupplier = supplier;

        decoratedSupplier = Bulkhead.decorateSupplier(cassandraBulkhead, decoratedSupplier);

        decoratedSupplier = Retry.decorateSupplier(cassandraRetry, decoratedSupplier);

        decoratedSupplier = CircuitBreaker.decorateSupplier(cassandraCircuitBreaker, decoratedSupplier);

        Supplier<CompletableFuture<T>> finalDecoratedSupplier = decoratedSupplier;
        return () -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                return finalDecoratedSupplier.get().whenComplete((result, throwable) -> {
                    sample.stop(Timer.builder("resilience.operation.duration")
                            .tag("operation", operationName)
                            .tag("success", throwable == null ? "true" : "false")
                            .register(meterRegistry));
                });
            } catch (Exception e) {
                sample.stop(Timer.builder("resilience.operation.duration")
                        .tag("operation", operationName)
                        .tag("success", "false")
                        .register(meterRegistry));
                throw e;
            }
        };
    }

    public void handleFailedWrite(ActionHistoryByAction activity, ActionHistoryByUser user, Exception exception, String operationId) {
        try {
            // Create comprehensive failed record
            FailedActivityRecord failedRecord = FailedActivityRecord.builder()
                    .activity(activity)
                    .user(user)
                    .exception(exception.getMessage())
                    .timestamp(Instant.now())
                    .operationId(operationId)
                    .retryCount(0)
                    .build();

            // Send to DLQ topic
            CompletableFuture<SendResult<String, FailedActivityRecord>> future = kafkaTemplate.send(DLQ_TOPIC, activity.getUserId(), failedRecord);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent failed activity to DLQ: {} operation: {}", activity.getId(), operationId);
                    metrics.incrementDlqMessages();
                } else {
                    log.error("Failed to send to DLQ: {} operation: {}", activity.getId(), operationId, ex);
                    // Store in Redis as last resort
                    storeInRedisAsLastResort(failedRecord, operationId);
                }
            });

        } catch (Exception e) {
            log.error("Failed to handle write failure for activity: {} operation: {}",
                    activity.getId(), operationId, e);
            // Last resort - structured logging
            logFailedActivity(activity, exception, operationId);
        }
    }

    private void storeInRedisAsLastResort(FailedActivityRecord failedRecord, String operationId) {
        try {
            String key = FAILED_ACTIVITIES_KEY_PREFIX + operationId;
            redisTemplate.opsForValue().set(key, failedRecord,
                    resilienceProperties.getFailedRecordRetention());
            log.info("Stored failed record in Redis as last resort: {}", operationId);
        } catch (Exception e) {
            log.error("Failed to store in Redis as last resort: {}", operationId, e);
        }
    }

    private void logFailedActivity(ActionHistoryByAction activity, Exception exception, String operationId) {
        // Log structured data cho easy parsing
        log.error("FAILED_ACTIVITY|{}|{}",
                activity.getUserId(),
                exception.getMessage());
    }

    private void incrementOperationCounter(String operation) {
        operationCounters.computeIfAbsent(operation, k -> new AtomicInteger(0)).incrementAndGet();
        meterRegistry.counter("user.activity.operations", "operation", operation).increment();
    }

    public void handleFailedKafkaPublish(ActionRecord actionRecord, Throwable ex) {
        log.debug("Failed to publish action to Kafka: {}", actionRecord.getUserId(), ex);
    }

    @Override
    public Health health() {
        try {
            CircuitBreaker.State state = cassandraCircuitBreaker.getState();
            boolean isHealthy = state == CircuitBreaker.State.CLOSED;

            Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();

            return healthBuilder
                    .withDetail("cassandra.circuit.breaker.state", state.toString())
                    .withDetail("active.requests", activeRequests.get())
                    .withDetail("last.health.check", lastHealthCheck.toString())
                    .withDetail("fallback.strategies.count", fallbackStrategies.size())
                    .withDetail("operation.counters", operationCounters)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

}
