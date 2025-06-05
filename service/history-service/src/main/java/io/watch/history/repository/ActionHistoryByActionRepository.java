package io.watch.history.repository;

import io.watch.history.entity.ActionHistoryByAction;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActionHistoryByActionRepository extends CassandraRepository<ActionHistoryByAction, Object> {

    Slice<ActionHistoryByAction> findByActionType(String actionType, Pageable pageable);

    @Query("SELECT * FROM action_history_by_action WHERE action_type = ?0 "
            + "AND action_timestamp > ?1 AND action_timestamp < ?2 ALLOW FILTERING")
    List<ActionHistoryByAction> findByActionTypeAndActionTimestampBetween(
            String actionType, Instant startDate, Instant endDate);
}