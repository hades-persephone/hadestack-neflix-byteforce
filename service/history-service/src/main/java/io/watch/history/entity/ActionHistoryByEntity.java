package io.watch.history.entity;

import io.watch.history.dto.ActionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("action_history_by_entity")
public class ActionHistoryByEntity {
    @PrimaryKeyColumn(name = "entity_type", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String entityType;

    @PrimaryKeyColumn(name = "entity_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String entityId;

    @PrimaryKeyColumn(name = "action_timestamp", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private Instant actionTimestamp;

    @Column("action_type")
    private String actionType;

    @Column("user_id")
    private String userId;

    @Column("details")
    private Map<String, String> details;

    @Column("source_ip")
    private String sourceIp;

    @Column("user_agent")
    private String userAgent;

    public static ActionHistoryByEntity fromActionRecord(ActionRecord record) {
        return ActionHistoryByEntity.builder()
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .actionTimestamp(record.getActionTimestamp())
                .actionType(record.getActionType())
                .userId(record.getUserId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }
}
