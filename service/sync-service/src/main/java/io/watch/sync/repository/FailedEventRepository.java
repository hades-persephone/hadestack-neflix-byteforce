package io.watch.sync.repository;

import io.lettuce.core.dynamic.annotation.Param;
import io.watch.sync.entity.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
    List<FailedEvent> findByStatusAndRetryCountLessThan(String status, int maxRetries);
    List<FailedEvent> findByStatusAndLastRetryAtBefore(String status, LocalDateTime time);

    @Query("SELECT tableName, COUNT(id) as count FROM FailedEvent WHERE status = :status GROUP BY tableName")
    List<Object[]> getFailureCountByTable(@Param("status") String status);}
