package io.watch.movie.config.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import io.watch.movie.service.CacheService;
import io.watch.movie.util.DataStruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    @Value("${cdc.topics.prefix}")
    private String topicPrefix;

    public void handleChangeEvent(ChangeEvent<SourceRecord, SourceRecord> changeEventRecord) {
        try {
            SourceRecord sourceRecord = changeEventRecord.value();

            String topic = sourceRecord.topic();
            String tableName = getTableName(topic);

            if (tableName == null) {
                log.warn("Could not extract table name from topic: {}", topic);
                return;
            }

            Object valueObj = sourceRecord.value();
            if (!(valueObj instanceof Struct structValue)) {
                log.warn("Expected value to be Struct but got {}", valueObj.getClass());
                return;
            }

            // Lấy operation
            String op = structValue.getString("op");

            Struct before = structValue.getStruct("before");
            Struct after = structValue.getStruct("after");

            Map<String, Object> changeEvent = new HashMap<>();
            changeEvent.put("operation", op);
            changeEvent.put("tableName", tableName);
            changeEvent.put("timestamp", Instant.now().toString());

            if (before != null) {
                changeEvent.put("before", DataStruct.structToMap(before));
            }

            if (after != null) {
                changeEvent.put("after", DataStruct.structToMap(after));
            }

            // Lấy id
            Object id = "d".equals(op) ? (before != null ? before.get("id") : null)
                    : (after != null ? after.get("id") : null);

            if (id != null) {
                changeEvent.put("entityId", id);
                String cacheKey = String.format("user:%s", id);
                cacheService.evict(cacheKey);

                if ("u".equals(op) || "d".equals(op)) {
                    String cachePattern = String.format("user:%s:*", id);
                    cacheService.evictPattern(cachePattern);
                }
            }

            String targetTopic = topicPrefix + "." + tableName;
            kafkaTemplate.send(targetTopic, changeEvent)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Failed to send change event to topic {}", targetTopic, error);
                        } else {
                            log.debug("Dispatched change event for table {} to topic {}", tableName, targetTopic);
                        }
                    });
            log.debug("Dispatched change event for table {} to topic {}", tableName, targetTopic);
        } catch (Exception e) {
            log.error("Error processing change event", e);
        }
    }

    private String getTableName(String topic) {
        String[] parts = topic.split("\\.");
        return parts.length >= 3 ? parts[2] : null;
    }

}
