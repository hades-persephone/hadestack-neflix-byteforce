package io.watch.sync.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.watch.sync.entity.SyncMapping;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class DataTransformer {
    public Map<String, Object> transform(JsonNode data, SyncMapping mapping) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, String> entry : mapping.getColumnMappings().entrySet()) {
            String sourceColumn = entry.getKey();
            String targetColumn = entry.getValue();
            JsonNode value = data.get(sourceColumn);

            Object convertedValue;

            if (value != null && value.isArray()) {
                if (value.size() == 3) {
                    convertedValue = Date.valueOf(LocalDate.of(
                            value.get(0).asInt(), value.get(1).asInt(), value.get(2).asInt()));
                } else if (value.size() >= 6) {
                    convertedValue = Timestamp.valueOf(LocalDateTime.of(
                            value.get(0).asInt(), value.get(1).asInt(), value.get(2).asInt(),
                            value.get(3).asInt(), value.get(4).asInt(), value.get(5).asInt()));
                } else {
                    convertedValue = null;
                }
            } else if ("id".equalsIgnoreCase(targetColumn) && value != null && value.isTextual()) {
                convertedValue = UUID.fromString(value.asText());
            } else {
                convertedValue = value != null && !value.isNull() ? value.asText() : null;
            }

            result.put(targetColumn, convertedValue);
        }

        return result;
    }
}