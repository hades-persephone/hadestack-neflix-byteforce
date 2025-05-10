package io.watch.sync.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Component to collect and expose metrics related to CDC processing
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    private static final String CDC_PROCESSED_EVENTS = "cdc.events.processed";
    private static final String CDC_FAILED_EVENTS = "cdc.events.failed";
    private static final String CDC_RETRY_EVENTS = "cdc.events.retry";
    private static final String CDC_DLT_EVENTS = "cdc.events.deadletter";
    private static final String CDC_CACHE_UPDATES = "cdc.cache.updates";
    private static final String CDC_EVENT_PROCESSING_TIME = "cdc.events.processing.time";

    private final Map<String, Counter> counterCache = new ConcurrentHashMap<>();

    /**
     * Increment counter for processed events
     */
    public void incrementProcessedEvents(String tableName, String operation) {
        try {
            String key = getCounterKey(CDC_PROCESSED_EVENTS, tableName, operation);
            Counter counter = counterCache.computeIfAbsent(key, k ->
                    Counter.builder(CDC_PROCESSED_EVENTS)
                            .tags(getTags(tableName, operation))
                            .description("Number of CDC events successfully processed")
                            .register(meterRegistry));
            counter.increment();
        } catch (Exception e) {
            log.error("Failed to increment processed events metric for table: {}, operation: {}", tableName, operation, e);
        }
    }

    /**
     * Increment counter for failed events (after max retries)
     */
    public void incrementFailedEvents(String tableName, String operation) {
        try {
            String key = getCounterKey(CDC_FAILED_EVENTS, tableName, operation);
            Counter counter = counterCache.computeIfAbsent(key, k ->
                    Counter.builder(CDC_FAILED_EVENTS)
                            .tags(getTags(tableName, operation))
                            .description("Number of CDC events that failed after max retries")
                            .register(meterRegistry));
            counter.increment();
        } catch (Exception e) {
            log.error("Failed to increment failed events metric for table: {}, operation: {}", tableName, operation, e);
        }
    }

    /**
     * Increment counter for event retries
     */
    public void incrementEventRetries(String tableName, String operation) {
        try {
            String key = getCounterKey(CDC_RETRY_EVENTS, tableName, operation);
            Counter counter = counterCache.computeIfAbsent(key, k ->
                    Counter.builder(CDC_RETRY_EVENTS)
                            .tags(getTags(tableName, operation))
                            .description("Number of CDC event retry attempts")
                            .register(meterRegistry));
            counter.increment();
        } catch (Exception e) {
            log.error("Failed to increment retry events metric for table: {}, operation: {}", tableName, operation, e);
        }
    }

    /**
     * Increment counter for events sent to dead letter topic
     */
    public void incrementDeadLetterEvents(String tableName, String operation) {
        try {
            String key = getCounterKey(CDC_DLT_EVENTS, tableName, operation);
            Counter counter = counterCache.computeIfAbsent(key, k ->
                    Counter.builder(CDC_DLT_EVENTS)
                            .tags(getTags(tableName, operation))
                            .description("Number of CDC events sent to dead letter topic")
                            .register(meterRegistry));
            counter.increment();
        } catch (Exception e) {
            log.error("Failed to increment dead letter events metric for table: {}, operation: {}", tableName, operation, e);
        }
    }

    /**
     * Increment counter for cache updates
     */
    public void incrementCacheUpdates(String tableName, String operation, boolean success) {
        try {
            String key = getCounterKey(CDC_CACHE_UPDATES, tableName, operation, String.valueOf(success));
            Counter counter = counterCache.computeIfAbsent(key, k ->
                    Counter.builder(CDC_CACHE_UPDATES)
                            .tags(Arrays.asList(
                                    Tag.of("table", safeTag(tableName)),
                                    Tag.of("operation", safeTag(operation)),
                                    Tag.of("result", success ? "success" : "failure")
                            ))
                            .description("Number of cache updates from CDC events")
                            .register(meterRegistry));
            counter.increment();
        } catch (Exception e) {
            log.error("Failed to increment cache updates metric for table: {}, operation: {}, success: {}", tableName, operation, success, e);
        }
    }

    public void recordProcessingTime(String tableName, String operation, long durationMs) {
        try {
            Timer.builder(CDC_EVENT_PROCESSING_TIME)
                    .tags(getTags(tableName, operation))
                    .description("Time taken to process CDC events")
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failed to record processing time for table: {}, operation: {}", tableName, operation, e);
        }
    }

    private List<Tag> getTags(String tableName, String operation) {
        return Arrays.asList(
                Tag.of("table", safeTag(tableName)),
                Tag.of("operation", safeTag(operation))
        );
    }

    private String getCounterKey(String metric, String... tags) {
        return metric + ":" + String.join(":", tags);
    }

    private String safeTag(String value) {
        return value != null ? value : "unknown";
    }
}