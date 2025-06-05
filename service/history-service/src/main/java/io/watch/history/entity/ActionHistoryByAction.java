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
@Table("action_history_by_action")
public class ActionHistoryByAction {
    @PrimaryKeyColumn(name = "action_type", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String actionType;

    @PrimaryKeyColumn(name = "action_timestamp", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Instant actionTimestamp;

    @Column("entity_type")
    private String entityType;

    @Column("entity_id")
    private String entityId;

    @Column("user_id")
    private String userId;

    @Column("details")
    private Map<String, String> details;

    @Column("source_ip")
    private String sourceIp;

    @Column("user_agent")
    private String userAgent;

    public String getId() {
        return userId + "_" + actionTimestamp;
    }

    public static ActionHistoryByAction fromActionRecord(ActionRecord record) {
        return ActionHistoryByAction.builder()
                .actionType(record.getActionType())
                .actionTimestamp(record.getActionTimestamp())
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .userId(record.getUserId())
                .details(record.getDetails())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }
}
