package io.watch.movie.repository;

import io.lettuce.core.dynamic.annotation.Param;
import io.watch.movie.entity.FailedMessage;
import io.watch.movie.handler.kafka.KafkaErrorHandlerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FailedMessageRepository extends JpaRepository<FailedMessage, UUID> {
    @Query("SELECT f FROM FailedMessage f WHERE f.topic = :topic AND f.createdAt >= :since")
    List<FailedMessage> findByTopicAndCreatedAtAfter(@Param("topic") String topic,
                                                     @Param("since") LocalDateTime since);

    List<FailedMessage> findByStatus(KafkaErrorHandlerService.FailedMessageStatus status);

    @Query("SELECT f.topic, COUNT(f) FROM FailedMessage f WHERE f.createdAt >= :since GROUP BY f.topic")
    List<Object[]> countByTopicSince(@Param("since") LocalDateTime since);

    @Query("SELECT f.errorType, COUNT(f) FROM FailedMessage f WHERE f.createdAt >= :since GROUP BY f.errorType")
    List<Object[]> countByErrorTypeSince(@Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE FailedMessage f SET f.status = :status, f.resolvedAt = :resolvedAt, f.resolvedBy = :resolvedBy WHERE f.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids,
                          @Param("status") KafkaErrorHandlerService.FailedMessageStatus status,
                          @Param("resolvedAt") LocalDateTime resolvedAt,
                          @Param("resolvedBy") String resolvedBy);
}
