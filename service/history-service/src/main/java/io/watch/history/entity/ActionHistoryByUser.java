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
@Table("action_history_by_user")
public class ActionHistoryByUser {
    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String userId;

    @PrimaryKeyColumn(name = "action_timestamp", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Instant actionTimestamp;

    @Column("entity_type")
    private String entityType;

    @Column("entity_id")
    private String entityId;

    @Column("action_type")
    private String actionType;

    @Column("device_type")
    private String deviceType;

    @Column("country")
    private String country;

    @Column("details")
    private Map<String, String> details;

    @Column("source_ip")
    private String sourceIp;

    @Column("user_agent")
    private String userAgent;

    public static ActionHistoryByUser fromActionRecord(ActionRecord record) {
        return ActionHistoryByUser.builder()
                .userId(record.getUserId())
                .actionTimestamp(record.getActionTimestamp())
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .actionType(record.getActionType())
                .details(record.getDetails())
                .deviceType(record.getDeviceType())
                .country(record.getCountry())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }
}