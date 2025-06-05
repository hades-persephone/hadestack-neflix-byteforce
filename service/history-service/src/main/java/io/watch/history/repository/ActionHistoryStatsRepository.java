package io.watch.history.repository;

import io.watch.history.entity.ActionHistoryStats;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionHistoryStatsRepository extends CassandraRepository<ActionHistoryStats, Object> {

    List<ActionHistoryStats> findByEntityTypeAndActionType(String entityType, String actionType);

    @Query("UPDATE action_history_stats SET count = count + 1 WHERE " +
            "entity_type = ?0 AND action_type = ?1 AND year_month = ?2")
    void incrementCount(String entityType, String actionType, String yearMonth);
}