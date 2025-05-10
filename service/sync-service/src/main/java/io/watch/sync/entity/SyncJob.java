package io.watch.sync.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "sync_jobs")
public class SyncJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobType;

    @Column(nullable = false)
    private String sourceTable;

    @Column(nullable = false)
    private String targetTable;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private String status;

    @Column
    private Long recordsProcessed = 0L;

    @Column
    private Long recordsFailed = 0L;

    @Column
    @Lob
    private String errorDetails;

}