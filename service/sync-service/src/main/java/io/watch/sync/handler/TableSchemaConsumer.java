package io.watch.sync.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.sync.entity.SyncMapping;
import io.watch.sync.repository.SyncMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableSchemaConsumer {

    private final SyncMappingRepository syncMappingRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "db.sync-mapping", groupId = "sync-mapping-group", containerFactory = "stringKafkaListenerContainerFactory")
    public void consumerTableSchema(String message) {
        try {
            List<SyncMapping> syncMappingList = objectMapper.readValue(message, new TypeReference<List<SyncMapping>>(){});
            log.info("Received {} schema mappings from Kafka", syncMappingList.size());
            for (SyncMapping syncMapping : syncMappingList) {
                Optional<SyncMapping> existingMappingOptional = syncMappingRepository.findBySourceTable(syncMapping.getSourceTable());
                if (existingMappingOptional.isPresent()) {
                    existingMappingOptional.get().setTargetTable(syncMapping.getTargetTable());
                    existingMappingOptional.get().setSourceIdColumn(syncMapping.getSourceIdColumn());
                    existingMappingOptional.get().setTargetIdColumn(syncMapping.getTargetIdColumn());
                    existingMappingOptional.get().setColumnMappings(syncMapping.getColumnMappings());
                    existingMappingOptional.get().setTransformationScript(syncMapping.getTransformationScript());
                    existingMappingOptional.get().setActive(syncMapping.isActive());
                    syncMappingRepository.save(existingMappingOptional.get());
                } else {
                    syncMappingRepository.save(syncMapping);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process table schema", e);
        }

    }

}
