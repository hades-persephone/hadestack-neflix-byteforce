package io.watch.history.repository;

import io.watch.history.entity.ActionHistoryByUser;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ActionHistoryByUserRepository extends CassandraRepository<ActionHistoryByUser, Object> {

    Slice<ActionHistoryByUser> findByKeyUserId(UUID key_userId, PageRequest pageRequest);

    @Query("SELECT * FROM action_history_by_user WHERE user_id = ?0 "
            + "AND login_time > ?1 AND login_time < ?2 ALLOW FILTERING")
    List<ActionHistoryByUser> findByKeyUserIdAndKeyLoginTimeBetween(String userId, Instant startDate, Instant endDate);

    @Query("SELECT * FROM action_history_by_user WHERE user_id = ?0 AND entity_type = ?1 ALLOW FILTERING")
    List<ActionHistoryByUser> findByUserIdAndEntityType(String userId, String entityType);
}
