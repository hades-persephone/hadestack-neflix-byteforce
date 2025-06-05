package io.watch.history.repository;

import io.watch.history.entity.ActionHistoryByEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActionHistoryByEntityRepository extends CassandraRepository<ActionHistoryByEntity, Object> {

    Slice<ActionHistoryByEntity> findByEntityTypeAndEntityId(
            String entityType, String entityId, Pageable pageable);

    @Query("SELECT * FROM action_history_by_entity WHERE entity_type = ?0 AND entity_id = ?1 "
            + "AND action_timestamp > ?2 AND action_timestamp < ?3 ALLOW FILTERING")
    List<ActionHistoryByEntity> findByEntityTypeAndEntityIdAndActionTimestampBetween(
            String entityType, String entityId, Instant startDate, Instant endDate);

    @Query("SELECT * FROM action_history_by_entity WHERE entity_type = ?0 AND entity_id = ?1 "
            + "AND action_type = ?2 ALLOW FILTERING")
    List<ActionHistoryByEntity> findByEntityTypeAndEntityIdAndActionType(
            String entityType, String entityId, String actionType);
}
