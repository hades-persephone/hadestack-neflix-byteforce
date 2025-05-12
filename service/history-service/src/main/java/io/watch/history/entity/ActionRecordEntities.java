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

public class ActionRecordEntities {

    /**
     * Entity for storing action history by entity type and ID
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table("action_history_by_entity")
    public static class ActionHistoryByEntity {
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

    /**
     * Entity for storing action history by user
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table("action_history_by_user")
    public static class ActionHistoryByUser {
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
                    .sourceIp(record.getSourceIp())
                    .userAgent(record.getUserAgent())
                    .build();
        }
    }

    /**
     * Entity for storing action history by action type
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table("action_history_by_action")
    public static class ActionHistoryByAction {
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

    /**
     * Entity for storing action history by time (year-month)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table("action_history_by_time")
    public static class ActionHistoryByTime {
        @PrimaryKeyColumn(name = "year_month", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String yearMonth;

        @PrimaryKeyColumn(name = "action_timestamp", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private Instant actionTimestamp;

        @Column("entity_type")
        private String entityType;

        @Column("entity_id")
        private String entityId;

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

        public static ActionHistoryByTime fromActionRecord(ActionRecord record) {
            return ActionHistoryByTime.builder()
                    .yearMonth(record.getYearMonth())
                    .actionTimestamp(record.getActionTimestamp())
                    .entityType(record.getEntityType())
                    .entityId(record.getEntityId())
                    .actionType(record.getActionType())
                    .userId(record.getUserId())
                    .details(record.getDetails())
                    .sourceIp(record.getSourceIp())
                    .userAgent(record.getUserAgent())
                    .build();
        }
    }

    /**
     * Entity for storing action stats
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table("action_history_stats")
    public static class ActionHistoryStats {
        @PrimaryKeyColumn(name = "entity_type", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String entityType;

        @PrimaryKeyColumn(name = "action_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String actionType;

        @PrimaryKeyColumn(name = "year_month", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        private String yearMonth;

        @Column("count")
        private Long count;
    }
}