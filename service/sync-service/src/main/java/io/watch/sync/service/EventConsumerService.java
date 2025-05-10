package io.watch.sync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.sync.entity.FailedEvent;
import io.watch.sync.entity.SyncMapping;
import io.watch.sync.exception.EventProcessingException;
import io.watch.sync.repository.FailedEventRepository;
import io.watch.sync.repository.SyncMappingRepository;
import io.watch.sync.util.DataTransformer;
import io.watch.sync.util.MetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumerService {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final FailedEventRepository failedEventRepository;
    private final DataTransformer dataTransformer;
    private final JdbcTemplate targetJdbcTemplate;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final MetricsCollector metricsCollector;

    @Value("${cdc.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${cdc.dlt.topic:cdc-dead-letter}")
    private String deadLetterTopic;

    @Value("${cdc.retry.interval-ms:1000}")
    private long retryIntervalMs;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private static final int ALERT_THRESHOLD = 5;
    private final SyncMappingRepository syncMappingRepository;

    @KafkaListener(
            topics = "#{'${cdc.topics.listen}'.split(',')}",
            containerFactory = "kafkaListenerContainerFactory",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Retryable(
            retryFor = {EventProcessingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2),
            listeners = "retryListener"
    )
    @Transactional
    public void consumeChangeEvent(Map<String, Object> changeEvent) {
        long startTime = System.currentTimeMillis();
        String operation = null;
        String tableName = null;
        String entityId = null;

        try {
            log.debug("Received change event: {}", changeEvent);

            operation = extractRequiredField(changeEvent, "operation", String.class);
            tableName = extractRequiredField(changeEvent, "tableName", String.class);

            switch (operation) {
                case "c":
                case "r":
                    entityId = handleCreateOrRead(tableName, changeEvent, operation);
                    break;
                case "u":
                    entityId = handleUpdate(tableName, changeEvent, operation);
                    break;
                case "d":
                    entityId = handleDelete(tableName, changeEvent, operation);
                    break;
                default:
                    log.warn("Unknown operation type: {}", operation);
                    break;
            }

            if (consecutiveFailures.get() > 0) {
                consecutiveFailures.set(0);
            }

            // Collect metrics
            metricsCollector.incrementProcessedEvents(tableName, operation);
            metricsCollector.recordProcessingTime(tableName, operation, System.currentTimeMillis() - startTime);

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation for event {}", changeEvent, e);
            handleFailedEvent(changeEvent, operation, tableName, UUID.fromString(entityId), "DB_CONSTRAINT", e.getMessage());
            throw new EventProcessingException("Database constraint violation", e);
        } catch (Exception e) {
            int failures = consecutiveFailures.incrementAndGet();
            if (failures >= ALERT_THRESHOLD) {
//                notificationService.sendAlert("CDC Processing Alert",
//                        String.format("Detected %d consecutive failures in CDC processing", failures));
                // Reset to avoid alert spam
                consecutiveFailures.set(0);
            }

            log.error("Error processing change event", e);
            handleFailedEvent(changeEvent, operation, tableName, UUID.fromString(entityId), "PROCESSING_ERROR", e.getMessage());
            throw new EventProcessingException("Failed to process event", e);
        }
    }

    @Recover
    public void recover(EventProcessingException exception, Map<String, Object> changeEvent) {
        String tableName = (String) changeEvent.get("tableName");
        String operation = (String) changeEvent.get("operation");
        tableName = tableName != null ? tableName : "unknown";
        operation = operation != null ? operation : "unknown";

        log.warn("Retries exhausted for event, sending to dead letter topic: {}", changeEvent);

        int maxDltRetries = 3;
        int dltRetryDelayMs = 1000;
        boolean sentToDlt = false;

        for (int attempt = 1; attempt <= maxDltRetries; attempt++) {
            try {
                kafkaTemplate.send(deadLetterTopic, changeEvent).get(); // Synchronous send for reliability
                log.info("Successfully sent failed event to DLT: {}", deadLetterTopic);
                metricsCollector.incrementDeadLetterEvents(tableName, operation);
                sentToDlt = true;
                break;
            } catch (Exception e) {
                log.error("Failed to send to dead letter topic (attempt {}/{}): {}", attempt, maxDltRetries, e.getMessage());
                metricsCollector.incrementFailedEvents(tableName, "dlt_send");
                if (attempt < maxDltRetries) {
                    try {
                        Thread.sleep(dltRetryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted during DLT retry", ie);
                        break;
                    }
                }
            }
        }

        if (!sentToDlt) {
            log.error("Critical failure: Could not send event to DLT after {} attempts: {}", maxDltRetries, changeEvent);
//            notificationService.sendAlert(
//                    "Critical CDC Failure",
//                    String.format("Failed to send event to DLT %s after %d retries: %s", deadLetterTopic, maxDltRetries, changeEvent)
//            );
        }
    }

    private String handleCreateOrRead(String tableName, Map<String, Object> changeEvent, String operation) {
        try {
            Object after = changeEvent.get("after");
            if (after == null) {
                log.warn("Missing 'after' data in create/read event for table: {}", tableName);
                metricsCollector.incrementFailedEvents(tableName, operation);
                throw new EventProcessingException("Missing 'after' data");
            }

            JsonNode afterNode = objectMapper.convertValue(after, JsonNode.class);
            JsonNode idNode = afterNode.get("id");
            if (idNode == null || !idNode.isTextual()) {
                log.warn("Missing or invalid 'id' in after data for table: {}", tableName);
                metricsCollector.incrementFailedEvents(tableName, operation);
                throw new EventProcessingException("Missing or invalid 'id'");
            }

            String entityId = idNode.asText();
            String cacheKey = String.format("%s:%s", tableName, entityId);
            processEntityData(tableName, afterNode, operation);
            cacheService.put(cacheKey, afterNode, Duration.ofHours(24), tableName, operation);
            log.info("Cached {} entity with ID {} after create/read event", tableName, entityId);
            return entityId;
        } catch (Exception e) {
            log.error("Error handling create/read event for table {}", tableName, e);
            metricsCollector.incrementFailedEvents(tableName, operation);
            throw new EventProcessingException("Failed to process create/read event", e);
        }
    }

    private String handleUpdate(String tableName, Map<String, Object> changeEvent, String operation) {
        try {
            JsonNode afterNode = objectMapper.convertValue(changeEvent.get("after"), JsonNode.class);
            if (afterNode == null || !afterNode.has("id")) {
                throw new EventProcessingException("Missing 'id' in 'after' data");
            }

            String entityId = afterNode.get("id").asText();
            String cacheKey = String.format("%s:%s", tableName, entityId);
            processEntityData(tableName, afterNode, operation);
            cacheService.put(cacheKey, afterNode, Duration.ofHours(24), tableName, operation);
            log.info("Updated cache for {} entity with ID {} after update event", tableName, entityId);
            return entityId;
        } catch (Exception e) {
            log.error("Error handling update event for table {}", tableName, e);
            throw new EventProcessingException("Failed to process update event", e);
        }
    }

    private String handleDelete(String tableName, Map<String, Object> changeEvent, String operation) {
        try {
            JsonNode beforeNode = objectMapper.convertValue(changeEvent.get("before"), JsonNode.class);
            if (beforeNode == null || !beforeNode.has("id")) {
                throw new EventProcessingException("Missing 'id' in 'before' data");
            }

            String entityId = beforeNode.get("id").asText();
            String cacheKey = String.format("%s:%d", tableName, entityId);
            cacheService.evict(cacheKey);
            String patternKey = String.format("%s:%d:*", tableName, entityId);
            cacheService.evictPattern(patternKey);

            log.info("Removed {} entity with ID {} from cache after delete event", tableName, entityId);
            return entityId;
        } catch (Exception e) {
            log.error("Error handling delete event for table {}", tableName, e);
            throw new EventProcessingException("Failed to process delete event", e);
        }
    }

    private void processEntityData(String tableName, JsonNode data, String operation) {
        try {
            // Retrieve mapping for the table
            SyncMapping mapping = syncMappingRepository.findBySourceTable(tableName)
                    .orElseThrow(() -> new EventProcessingException("No mapping found for table: " + tableName));

            Map<String, Object> transformedData = dataTransformer.transform(data, mapping);
            String sql = buildSql(operation, mapping, transformedData);
            targetJdbcTemplate.update(sql, transformedData.values().toArray());
            log.debug("Applied {} operation on table {}: SQL = {}", operation, mapping, sql);
        } catch (Exception e) {
            log.error("Error processing entity data for table {} and operation {}", tableName, operation, e);
            throw new EventProcessingException("Failed to process entity data for table: " + tableName, e);
        }
    }

    private String buildSql(String operation, SyncMapping mapping, Map<String, Object> data) {
        String targetTable = mapping.getTargetTable();
        switch (operation) {
            case "c":
            case "r":
                String columns = String.join(", ", data.keySet());
                String placeholders = String.join(", ", Collections.nCopies(data.size(), "?"));
                return String.format("INSERT INTO %s (%s) VALUES (%s)", targetTable, columns, placeholders);
            case "u":
                String setClause = data.keySet().stream()
                        .filter(key -> !"id".equals(key))
                        .map(key -> key + " = ?")
                        .collect(Collectors.joining(", "));
                return String.format("UPDATE %s SET %s WHERE id = ?", targetTable, setClause);

            case "d":
                return String.format("DELETE FROM %s WHERE id = ?", targetTable);

            default:
                throw new EventProcessingException("Unsupported operation: " + operation);
        }
    }

    private void handleFailedEvent(Map<String, Object> changeEvent, String operation, String tableName,
                                   UUID entityId, String errorType, String errorMessage) {
        try {
            FailedEvent failedEvent = new FailedEvent();
            failedEvent.setEventData(objectMapper.writeValueAsString(changeEvent));
            failedEvent.setOperation(operation);
            failedEvent.setTableName(tableName);
            failedEvent.setEntityId(entityId);
            failedEvent.setErrorType(errorType);
            failedEvent.setErrorMessage(errorMessage);
            failedEvent.setCreatedAt(LocalDateTime.now());
            failedEvent.setRetryCount(0);
            failedEvent.setStatus("PENDING");

            failedEventRepository.save(failedEvent);

            log.info("Saved failed event to database for later retry. ID: {}", failedEvent.getId());
        } catch (Exception e) {
            log.error("Failed to save failed event to database", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T extractRequiredField(Map<String, Object> event, String fieldName, Class<T> type) {
        Object value = event.get(fieldName);
        if (value == null) {
            throw new EventProcessingException("Required field '" + fieldName + "' is missing");
        }
        if (!type.isInstance(value)) {
            throw new EventProcessingException("Field '" + fieldName + "' has invalid type");
        }
        return (T) value;
    }
}