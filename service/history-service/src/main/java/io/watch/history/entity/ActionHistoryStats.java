package io.watch.history.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("action_history_stats")
public class ActionHistoryStats {
    @PrimaryKeyColumn(name = "entity_type", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String entityType;

    @PrimaryKeyColumn(name = "action_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String actionType;

    @PrimaryKeyColumn(name = "year_month", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private String yearMonth;

    @Column("count")
    private Long count;
}
