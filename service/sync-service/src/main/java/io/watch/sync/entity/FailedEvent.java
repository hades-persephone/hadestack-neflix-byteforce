package io.watch.sync.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "failed_events")
public class FailedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String eventData;

    private String operation;

    private String tableName;

    private UUID entityId;

    private String errorType;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime lastRetryAt;

    private Integer retryCount;

    private String status; // PENDING, RETRYING, RESOLVED, FAILED
}