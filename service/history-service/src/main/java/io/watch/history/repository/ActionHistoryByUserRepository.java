package io.watch.history.repository;

import io.watch.history.entity.ActionHistoryByUser;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.List;

public interface ActionHistoryByUserRepository extends CassandraRepository<ActionHistoryByUser, Object> {

    Slice<ActionHistoryByUser> findByUserId(String userId, Pageable pageable);

    @Query("SELECT * FROM action_history_by_user WHERE user_id = ?0 "
            + "AND action_timestamp > ?1 AND action_timestamp < ?2 ALLOW FILTERING")
    List<ActionHistoryByUser> findByUserIdAndActionTimestampBetween(
            String userId, Instant startDate, Instant endDate);

    @Query("SELECT * FROM action_history_by_user WHERE user_id = ?0 AND entity_type = ?1 ALLOW FILTERING")
    List<ActionHistoryByUser> findByUserIdAndEntityType(String userId, String entityType);
}
