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
public class WatchHistory extends EntityBase {

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

    @Min(value = 1, message = "Watch count must be at least 1")
    @Column(name = "watch_count")
    @Schema(description = "Number of times watched", example = "1")
    private Integer watchCount = 1;

}
