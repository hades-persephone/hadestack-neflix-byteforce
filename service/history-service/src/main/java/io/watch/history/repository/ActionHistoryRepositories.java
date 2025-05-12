package io.watch.history.repository;

import io.watch.history.entity.ActionRecordEntities.*;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

public interface ActionHistoryRepositories {

    @Repository
    interface ActionHistoryByEntityRepository extends CassandraRepository<ActionHistoryByEntity, Object> {

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

    @Repository
    interface ActionHistoryByUserRepository extends CassandraRepository<ActionHistoryByUser, Object> {

        Slice<ActionHistoryByUser> findByUserId(String userId, Pageable pageable);

        @Query("SELECT * FROM action_history_by_user WHERE user_id = ?0 "
                + "AND action_timestamp > ?1 AND action_timestamp < ?2 ALLOW FILTERING")
        List<ActionHistoryByUser> findByUserIdAndActionTimestampBetween(
                String userId, Instant startDate, Instant endDate);

        @Query("SELECT * FROM action_history_by_user WHERE user_id = ?0 AND entity_type = ?1 ALLOW FILTERING")
        List<ActionHistoryByUser> findByUserIdAndEntityType(String userId, String entityType);
    }

    @Repository
    interface ActionHistoryByActionRepository extends CassandraRepository<ActionHistoryByAction, Object> {

        Slice<ActionHistoryByAction> findByActionType(String actionType, Pageable pageable);

        @Query("SELECT * FROM action_history_by_action WHERE action_type = ?0 "
                + "AND action_timestamp > ?1 AND action_timestamp < ?2 ALLOW FILTERING")
        List<ActionHistoryByAction> findByActionTypeAndActionTimestampBetween(
                String actionType, Instant startDate, Instant endDate);
    }

    @Repository
    interface ActionHistoryByTimeRepository extends CassandraRepository<ActionHistoryByTime, Object> {

        Slice<ActionHistoryByTime> findByYearMonth(String yearMonth, Pageable pageable);

        @Query("SELECT * FROM action_history_by_time WHERE year_month = ?0 "
                + "AND action_timestamp > ?1 AND action_timestamp < ?2 ALLOW FILTERING")
        List<ActionHistoryByTime> findByYearMonthAndActionTimestampBetween(
                String yearMonth, Instant startDate, Instant endDate);

        @Query("SELECT * FROM action_history_by_time WHERE year_month = ?0 AND entity_type = ?1 ALLOW FILTERING")
        List<ActionHistoryByTime> findByYearMonthAndEntityType(String yearMonth, String entityType);
    }

    @Repository
    interface ActionHistoryStatsRepository extends CassandraRepository<ActionHistoryStats, Object> {

        List<ActionHistoryStats> findByEntityTypeAndActionType(String entityType, String actionType);

        @Query("UPDATE action_history_stats SET count = count + 1 WHERE " +
                "entity_type = ?0 AND action_type = ?1 AND year_month = ?2")
        void incrementCount(String entityType, String actionType, String yearMonth);
    }
}