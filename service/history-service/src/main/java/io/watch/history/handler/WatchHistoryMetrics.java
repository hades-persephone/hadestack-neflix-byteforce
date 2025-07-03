package io.watch.history.handler;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class WatchHistoryMetrics {

    private final MeterRegistry meterRegistry;

    // Counters for successful operations
    private final Counter successfulSavesCounter;
    private final Counter successfulProgressUpdatesCounter;
    private final Counter successfulBatchSavesCounter;

    // Counters for failed operations
    private final Counter failedSavesCounter;
    private final Counter failedProgressUpdatesCounter;
    private final Counter failedBatchSavesCounter;

    // Counters for fallback operations
    private final Counter circuitBreakerFallbacksCounter;
    private final Counter progressUpdateFallbacksCounter;
    private final Counter batchFallbacksCounter;

    // Timers for operation durations
    private final Timer saveOperationTimer;
    private final Timer progressUpdateTimer;
    private final Timer batchSaveTimer;

    // Gauges for current state
    private final AtomicLong activeSaveOperations;
    private final AtomicLong activeProgressUpdates;
    private final AtomicLong activeBatchOperations;
    private final AtomicLong totalFallbackStorageSize;

    // Health metrics
    private final AtomicLong lastSuccessfulOperation;
    private final AtomicLong consecutiveFailures;

    public WatchHistoryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.successfulSavesCounter = Counter.builder("watch.history.saves.successful")
                .description("Number of successful action history saves")
                .register(meterRegistry);

        this.successfulProgressUpdatesCounter = Counter.builder("watch.history.progress.successful")
                .description("Number of successful watch progress updates")
                .register(meterRegistry);

        this.successfulBatchSavesCounter = Counter.builder("watch.history.batch.successful")
                .description("Number of successful batch saves")
                .register(meterRegistry);

        this.failedSavesCounter = Counter.builder("watch.history.saves.failed")
                .description("Number of failed action history saves")
                .register(meterRegistry);

        this.failedProgressUpdatesCounter = Counter.builder("watch.history.progress.failed")
                .description("Number of failed watch progress updates")
                .register(meterRegistry);

        this.failedBatchSavesCounter = Counter.builder("watch.history.batch.failed")
                .description("Number of failed batch saves")
                .register(meterRegistry);

        this.circuitBreakerFallbacksCounter = Counter.builder("watch.history.fallback.circuit_breaker")
                .description("Number of circuit breaker triggered fallbacks")
                .register(meterRegistry);

        this.progressUpdateFallbacksCounter = Counter.builder("watch.history.fallback.progress")
                .description("Number of progress update fallbacks")
                .register(meterRegistry);

        this.batchFallbacksCounter = Counter.builder("watch.history.fallback.batch")
                .description("Number of batch operation fallbacks")
                .register(meterRegistry);

        // Initialize timers
        this.saveOperationTimer = Timer.builder("watch.history.saves.duration")
                .description("Duration of save operations")
                .register(meterRegistry);

        this.progressUpdateTimer = Timer.builder("watch.history.progress.duration")
                .description("Duration of progress update operations")
                .register(meterRegistry);

        this.batchSaveTimer = Timer.builder("watch.history.batch.duration")
                .description("Duration of batch save operations")
                .register(meterRegistry);

        // Initialize atomic longs for gauges
        this.activeSaveOperations = new AtomicLong(0);
        this.activeProgressUpdates = new AtomicLong(0);
        this.activeBatchOperations = new AtomicLong(0);
        this.totalFallbackStorageSize = new AtomicLong(0);
        this.lastSuccessfulOperation = new AtomicLong(Instant.now().toEpochMilli());
        this.consecutiveFailures = new AtomicLong(0);

        // Register gauges
        Gauge.builder("watch.history.operations.active.saves", activeSaveOperations, AtomicLong::get)
                .description("Number of currently active save operations")
                .register(meterRegistry);

        Gauge.builder("watch.history.operations.active.progress", activeProgressUpdates, AtomicLong::get)
                .description("Number of currently active progress updates")
                .register(meterRegistry);

        Gauge.builder("watch.history.operations.active.batch", activeBatchOperations, AtomicLong::get)
                .description("Number of currently active batch operations")
                .register(meterRegistry);

        Gauge.builder("watch.history.fallback.storage.size", totalFallbackStorageSize, AtomicLong::get)
                .description("Total size of items in fallback storage")
                .register(meterRegistry);

        Gauge.builder("watch.history.health.last_successful_operation", lastSuccessfulOperation, AtomicLong::get)
                .description("Timestamp of last successful operation")
                .register(meterRegistry);

        Gauge.builder("watch.history.health.consecutive_failures", consecutiveFailures, AtomicLong::get)
                .description("Number of consecutive failures")
                .register(meterRegistry);
    }

    // Success counter methods
    public void incrementSuccessfulSaves() {
        successfulSavesCounter.increment();
        lastSuccessfulOperation.set(Instant.now().toEpochMilli());
        consecutiveFailures.set(0);
        log.debug("Incremented successful saves counter");
    }

    public void incrementSuccessfulProgressUpdates() {
        successfulProgressUpdatesCounter.increment();
        lastSuccessfulOperation.set(Instant.now().toEpochMilli());
        consecutiveFailures.set(0);
        log.debug("Incremented successful progress updates counter");
    }

    public void incrementSuccessfulBatchSaves() {
        successfulBatchSavesCounter.increment();
        lastSuccessfulOperation.set(Instant.now().toEpochMilli());
        consecutiveFailures.set(0);
        log.debug("Incremented successful batch saves counter");
    }

    // Failure counter methods
    public void incrementFailedSaves() {
        failedSavesCounter.increment();
        consecutiveFailures.incrementAndGet();
        log.debug("Incremented failed saves counter");
    }

    public void incrementFailedProgressUpdates() {
        failedProgressUpdatesCounter.increment();
        consecutiveFailures.incrementAndGet();
        log.debug("Incremented failed progress updates counter");
    }

    public void incrementFailedBatchSaves() {
        failedBatchSavesCounter.increment();
        consecutiveFailures.incrementAndGet();
        log.debug("Incremented failed batch saves counter");
    }

    // Fallback counter methods
    public void incrementCircuitBreakerFallbacks() {
        circuitBreakerFallbacksCounter.increment();
        log.debug("Incremented circuit breaker fallbacks counter");
    }

    public void incrementProgressUpdateFallbacks() {
        progressUpdateFallbacksCounter.increment();
        log.debug("Incremented progress update fallbacks counter");
    }

    public void incrementBatchFallbacks() {
        batchFallbacksCounter.increment();
        log.debug("Incremented batch fallbacks counter");
    }

    // Timer methods
    public Timer.Sample startSaveTimer() {
        activeSaveOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopSaveTimer(Timer.Sample sample) {
        sample.stop(saveOperationTimer);
        activeSaveOperations.decrementAndGet();
    }

    public Timer.Sample startProgressUpdateTimer() {
        activeProgressUpdates.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopProgressUpdateTimer(Timer.Sample sample) {
        sample.stop(progressUpdateTimer);
        activeProgressUpdates.decrementAndGet();
    }

    public Timer.Sample startBatchSaveTimer() {
        activeBatchOperations.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopBatchSaveTimer(Timer.Sample sample) {
        sample.stop(batchSaveTimer);
        activeBatchOperations.decrementAndGet();
    }

    // Convenience methods for timing operations
    public <T> T recordSaveOperation(java.util.function.Supplier<T> operation) throws Exception {
        return saveOperationTimer.recordCallable(() -> {
            activeSaveOperations.incrementAndGet();
            try {
                return operation.get();
            } finally {
                activeSaveOperations.decrementAndGet();
            }
        });
    }

    public <T> T recordProgressUpdateOperation(java.util.function.Supplier<T> operation) throws Exception {
        return progressUpdateTimer.recordCallable(() -> {
            activeProgressUpdates.incrementAndGet();
            try {
                return operation.get();
            } finally {
                activeProgressUpdates.decrementAndGet();
            }
        });
    }

    public <T> T recordBatchSaveOperation(java.util.function.Supplier<T> operation) throws Exception {
        return batchSaveTimer.recordCallable(() -> {
            activeBatchOperations.incrementAndGet();
            try {
                return operation.get();
            } finally {
                activeBatchOperations.decrementAndGet();
            }
        });
    }

    // Fallback storage tracking
    public void incrementFallbackStorageSize(long itemCount) {
        totalFallbackStorageSize.addAndGet(itemCount);
        log.debug("Incremented fallback storage size by {}", itemCount);
    }

    public void decrementFallbackStorageSize(long itemCount) {
        totalFallbackStorageSize.addAndGet(-itemCount);
        log.debug("Decremented fallback storage size by {}", itemCount);
    }


    // Health check methods
    public boolean isHealthy() {
        long timeSinceLastSuccess = Instant.now().toEpochMilli() - lastSuccessfulOperation.get();
        long maxAllowedTime = Duration.ofMinutes(5).toMillis(); // 5 minutes threshold
        long maxConsecutiveFailures = 10;

        return timeSinceLastSuccess < maxAllowedTime &&
                consecutiveFailures.get() < maxConsecutiveFailures;
    }

    public long getTimeSinceLastSuccessfulOperation() {
        return Instant.now().toEpochMilli() - lastSuccessfulOperation.get();
    }

    public long getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    // Get current metric values
    public double getSuccessfulSavesCount() {
        return successfulSavesCounter.count();
    }

    public double getFailedSavesCount() {
        return failedSavesCounter.count();
    }

    public double getSuccessRate() {
        double total = getSuccessfulSavesCount() + getFailedSavesCount();
        return total > 0 ? (getSuccessfulSavesCount() / total) * 100 : 0;
    }

    public double getAverageSaveTime() {
        return saveOperationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public double getAverageProgressUpdateTime() {
        return progressUpdateTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public double getAverageBatchSaveTime() {
        return batchSaveTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // Reset methods (useful for testing)
    public void resetMetrics() {
        log.info("Resetting all watch history metrics");
        activeSaveOperations.set(0);
        activeProgressUpdates.set(0);
        activeBatchOperations.set(0);
        totalFallbackStorageSize.set(0);
        lastSuccessfulOperation.set(Instant.now().toEpochMilli());
        consecutiveFailures.set(0);
    }

    // Logging method for periodic metric reporting
    public void logCurrentMetrics() {

    }

    public void incrementFallbackSuccess(String simpleName) {
    }

    public void incrementDlqMessages() {
    }

    public void incrementFallbackFailure(String simpleName) {
    }
}