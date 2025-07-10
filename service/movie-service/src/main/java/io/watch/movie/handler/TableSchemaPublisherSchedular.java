package io.watch.movie.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.movie.dto.SyncMappingDto;
import io.watch.movie.handler.kafka.KafkaErrorHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableSchemaPublisherSchedular {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaErrorHandlerService kafkaErrorHandlerService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 * * 0")
    public void publishTableSchema() {
        try {
            List<SyncMappingDto> syncMappings = getAllSchemas();
            if(!syncMappings.isEmpty()) {
                String message = objectMapper.writeValueAsString(syncMappings);
                kafkaTemplate.send("db.sync-mapping", "sync-mapping", message)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                Map<String, Object> headers = new HashMap<>();
                                headers.put("timestamp", LocalDateTime.now());
                                headers.put("source", this.getClass().getSimpleName());
                                kafkaErrorHandlerService.handlerKafkaError("db.sync-mapping", "sync-mapping", message, ex, headers);
                                log.error("❌ Kafka send failed: {} - {}, reason: {}", "sync-mapping", message, ex.getMessage());
                            } else {
                                log.info("✅ Kafka sent: {} - {}", "sync-mapping", message);
                            }
                        });

            }
        } catch (RuntimeException | JsonProcessingException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SyncMappingDto> getAllSchemas() throws SQLException {
        List<SyncMappingDto> syncMappings = new ArrayList<>();
        assert jdbcTemplate.getDataSource() != null;
        DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            SyncMappingDto syncMapping = new SyncMappingDto();
            syncMapping.setSourceTable(tableName);
            syncMapping.setTargetTable(tableName);
            syncMapping.setActive(true);

            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            Map<String, String> columnMap = new HashMap<>();
            String primaryKey = null;
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                columnMap.put(columnName, columnName);
            }

            ResultSet pk = metaData.getPrimaryKeys(null, null, tableName);
            if(pk.next()) {
                primaryKey = pk.getString("COLUMN_NAME");
            }
            syncMapping.setSourceIdColumn(primaryKey);
            syncMapping.setTargetIdColumn(primaryKey);
            syncMapping.setColumnMappings(columnMap);
            syncMappings.add(syncMapping);
        }
        return syncMappings;
    }

}
