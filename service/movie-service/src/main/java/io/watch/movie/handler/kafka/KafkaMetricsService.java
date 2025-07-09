package io.watch.movie.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class KafkaMetricsService {

    private final ConcurrentHashMap<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> topicErrorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> retryCounters = new ConcurrentHashMap<>();

    public void recordError(KafkaErrorHandlerService.ErrorType errorType, String originalTopic) {
        String key = errorType.name();
        errorCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();

        String topicKey = originalTopic + ":" + errorType.name();
        topicErrorCounters.computeIfAbsent(topicKey, k -> new AtomicLong(0)).incrementAndGet();

        log.debug("record error for topic: {}, key: {}", topicKey, key);
    }

    public void recordRetrySuccess(String originalTopic) {
        String key = originalTopic + ":retry_success";
        retryCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
    public void recordDlqSend(String dlqTopic) {
        String key = dlqTopic + ":dlq_send";
        retryCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
    public void recordRetrySkippedMessage(String topic) {
        String key = topic + ":skipped_success";
        retryCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
    public Map<String, Object> getErrorStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Long> errorStats = new HashMap<>();
        errorCounters.forEach((key, errorCounter) -> errorStats.put(key, errorCounter.get()));
        stats.put("errorsByType", errorStats);

        Map<String, Long> topicStats = new HashMap<>();
        topicErrorCounters.forEach((key, errorCounter) -> topicStats.put(key, errorCounter.get()));
        stats.put("errorsByType", topicStats);

        Map<String, Long> retryStats = new HashMap<>();
        retryCounters.forEach((key, errorCounter) -> retryStats.put(key, errorCounter.get()));
        stats.put("errorsByType", retryStats);

        return stats;
    }

    public void resetCounters() {
        errorCounters.clear();
        topicErrorCounters.clear();
        retryCounters.clear();
        log.info("Kafka metrics counters reset");
    }

    public long getErrorCount(KafkaErrorHandlerService.ErrorType errorType, String topic) {
        String key = topic + ":" + errorType.name();
        return topicErrorCounters.getOrDefault(key, new AtomicLong(0)).get();
    }

    public long getTotalErrorCount(String topic) {
        return topicErrorCounters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(topic + ":"))
                .mapToLong(entry -> entry.getValue().get())
                .sum();
    }

    public void recordSkippedMessage(String originalTopic) {
        String key = originalTopic + ":skipped";
        retryCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
}
