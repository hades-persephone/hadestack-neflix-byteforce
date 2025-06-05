package io.watch.history.repository;

import io.watch.history.entity.ActionHistoryByTime;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActionHistoryByTimeRepository extends CassandraRepository<ActionHistoryByTime, Object> {

    Slice<ActionHistoryByTime> findByYearMonth(String yearMonth, Pageable pageable);

    @Query("SELECT * FROM action_history_by_time WHERE year_month = ?0 "
            + "AND action_timestamp > ?1 AND action_timestamp < ?2 ALLOW FILTERING")
    List<ActionHistoryByTime> findByYearMonthAndActionTimestampBetween(
            String yearMonth, Instant startDate, Instant endDate);

    @Query("SELECT * FROM action_history_by_time WHERE year_month = ?0 AND entity_type = ?1 ALLOW FILTERING")
    List<ActionHistoryByTime> findByYearMonthAndEntityType(String yearMonth, String entityType);
}
