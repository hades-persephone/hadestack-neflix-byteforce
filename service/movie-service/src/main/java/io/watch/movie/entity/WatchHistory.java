package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.VideoQuality;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "watch_history")
@Schema(description = "WatchHistory entity representing a user's watch history")
public class WatchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the watch history", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id")
    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Column(name = "movie_id")
    @Schema(description = "ID of the movie watched", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID movieId;

    @Column(name = "episode_id")
    @Schema(description = "ID of the episode watched", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID episodeId;

    @Column(name = "watched_at")
    @CreationTimestamp
    @Schema(description = "Timestamp when watched", example = "2025-04-10T10:00:00Z")
    private LocalDateTime watchedAt;

    @Min(value = 0, message = "Watch duration cannot be negative")
    @Column(name = "watch_duration_seconds")
    @Schema(description = "Watch duration in seconds", example = "3600")
    private Integer watchDurationSeconds;

    @Column(name = "completed")
    @Schema(description = "Whether the content was fully watched", example = "false")
    private Boolean completed = false;

    @Min(value = 0, message = "Last position cannot be negative")
    @Column(name = "last_position_seconds")
    @Schema(description = "Last position in seconds", example = "1800")
    private Integer lastPositionSeconds;

    @Size(max = 50, message = "Device type cannot exceed 50 characters")
    @Column(name = "device_type")
    @Schema(description = "Device type", example = "Smartphone")
    private String deviceType;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Column(name = "ip_address")
    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_watched")
    @Schema(description = "Quality watched", example = "HD")
    private VideoQuality qualityWatched;

    @Column(name = "created_at")
    @CreationTimestamp
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @UpdateTimestamp
    @Schema(description = "Deletion timestamp", example = "null")
    private LocalDateTime deletedAt;

    @Min(value = 1, message = "Watch count must be at least 1")
    @Column(name = "watch_count")
    @Schema(description = "Number of times watched", example = "1")
    private Integer watchCount = 1;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
