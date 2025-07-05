package io.watch.history.entity;

import io.watch.history.dto.ActionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("action_history_by_entity")
public class ActionHistoryByEntity {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "action_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String actionType;

    @PrimaryKeyColumn(name = "action_timestamp", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant actionTimestamp;

    @PrimaryKeyColumn(name = "entity_type", ordinal = 3, type = PrimaryKeyType.PARTITIONED)
    private String entityType;

    @PrimaryKeyColumn(name = "entity_id", ordinal = 4, type = PrimaryKeyType.PARTITIONED)
    private String entityId;

    @Column("details")
    private Map<String, String> details;

    @Column("source_ip")
    private String sourceIp;

    @Column("user_agent")
    private String userAgent;

    public static ActionHistoryByEntity fromActionRecord(ActionRecord record) {
        return ActionHistoryByEntity.builder()
                .userId(record.getUserId())
                .actionType(record.getActionType())
                .actionTimestamp(record.getActionTimestamp())
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }
}
