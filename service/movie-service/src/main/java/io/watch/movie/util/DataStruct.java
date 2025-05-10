package io.watch.movie.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class DataStruct {

    public Map<String, Object> structToMap(Struct struct) {
        Map<String, Object> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper(); // For JSON parsing
        ZoneId defaultZone = ZoneId.of("UTC"); // Consistent timezone

        for (Field field : struct.schema().fields()) {
            Object value = struct.get(field);
            String fieldType = field.schema().name(); // Debezium custom types
            Schema.Type baseType = field.schema().type(); // Base Avro/Connect type

            if (value != null) {
                // Handle Debezium-specific types
                if (fieldType != null) {
                    switch (fieldType) {
                        case "io.debezium.time.Date":
                            try {
                                value = LocalDate.ofEpochDay(((Number) value).longValue());
                            } catch (Exception e) {
                                value = null; // Invalid date
                            }
                            break;
                        case "io.debezium.time.Timestamp":
                            try {
                                value = Instant.ofEpochMilli(((Number) value).longValue())
                                        .atZone(defaultZone)
                                        .toLocalDateTime();
                            } catch (Exception e) {
                                value = null; // Invalid timestamp
                            }
                            break;
                        case "io.debezium.time.MicroTimestamp":
                            try {
                                long micros = ((Number) value).longValue();
                                value = Instant.ofEpochMilli(micros / 1000)
                                        .atZone(defaultZone)
                                        .toLocalDateTime();
                            } catch (Exception e) {
                                value = null; // Invalid microtimestamp
                            }
                            break;
                        case "io.debezium.time.NanoTimestamp":
                            try {
                                long nanos = ((Number) value).longValue();
                                value = Instant.ofEpochSecond(nanos / 1_000_000_000, nanos % 1_000_000_000)
                                        .atZone(defaultZone)
                                        .toLocalDateTime();
                            } catch (Exception e) {
                                value = null; // Invalid nanotimestamp
                            }
                            break;
                        case "io.debezium.time.ZonedTimestamp":
                            try {
                                value = Instant.parse(value.toString())
                                        .atZone(defaultZone)
                                        .toLocalDateTime();
                            } catch (DateTimeParseException e) {
                                value = null; // Invalid zoned timestamp
                            }
                            break;
                        case "io.debezium.time.Time":
                            try {
                                value = LocalTime.ofNanoOfDay(((Number) value).longValue() * 1000);
                            } catch (Exception e) {
                                value = null; // Invalid time
                            }
                            break;
                        case "io.debezium.time.MicroTime":
                            try {
                                long microsTime = ((Number) value).longValue();
                                value = LocalTime.ofNanoOfDay(microsTime * 1000);
                            } catch (Exception e) {
                                value = null; // Invalid microtime
                            }
                            break;
                        case "io.debezium.data.Uuid":
                            try {
                                value = UUID.fromString(value.toString());
                            } catch (IllegalArgumentException e) {
                                value = null; // Invalid UUID
                            }
                            break;
                        case "io.debezium.data.Json":
                            try {
                                value = mapper.readValue(value.toString(), Map.class);
                            } catch (JsonProcessingException e) {
                                value = value.toString(); // Keep as string if parsing fails
                            }
                            break;
                        case "io.debezium.data.Enum":
                            value = value.toString(); // Enum values are strings
                            break;
                        case "io.debezium.time.Interval":
                            value = value.toString(); // ISO-8601 duration string
                            break;
                        case "io.debezium.data.Decimal":
                            try {
                                value = new java.math.BigDecimal(value.toString());
                            } catch (NumberFormatException e) {
                                value = null; // Invalid decimal
                            }
                            break;
                    }
                }

                // Handle Kafka Connect base types
                if (baseType == Schema.Type.BYTES && value instanceof byte[]) {
                    value = Base64.getEncoder().encodeToString((byte[]) value);
                } else if (baseType == Schema.Type.STRUCT && value instanceof Struct) {
                    value = structToMap((Struct) value); // Recursive for nested Struct
                } else if (baseType == Schema.Type.ARRAY && value instanceof List) {
                    List<Object> list = (List<Object>) value;
                    List<Object> convertedList = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Struct) {
                            convertedList.add(structToMap((Struct) item));
                        } else {
                            convertedList.add(item); // Keep other array elements as-is
                        }
                    }
                    value = convertedList;
                } else if (baseType == Schema.Type.MAP && value instanceof Map) {
                    Map<Object, Object> map = (Map<Object, Object>) value;
                    Map<Object, Object> convertedMap = new HashMap<>();
                    for (Map.Entry<Object, Object> entry : map.entrySet()) {
                        Object key = entry.getKey();
                        Object val = entry.getValue();
                        if (val instanceof Struct) {
                            val = structToMap((Struct) val);
                        }
                        convertedMap.put(key, val);
                    }
                    value = convertedMap;
                }

            }

            result.put(field.name(), value);
        }

        return result;
    }

}
