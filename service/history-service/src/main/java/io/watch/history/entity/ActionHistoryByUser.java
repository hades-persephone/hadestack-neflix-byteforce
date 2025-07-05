package io.watch.history.entity;

import io.watch.history.dto.ActionRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("action_history_by_user")
public class ActionHistoryByUser {

    @PrimaryKey
    private UserLoginKey key;

    @Column("entity_type")
    private String entityType;

    @Column("entity_id")
    private String entityId;

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
                .key(record.getKey())
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .details(record.getDetails())
                .deviceType(record.getDeviceType())
                .country(record.getCountry())
                .sourceIp(record.getSourceIp())
                .userAgent(record.getUserAgent())
                .build();
    }
}