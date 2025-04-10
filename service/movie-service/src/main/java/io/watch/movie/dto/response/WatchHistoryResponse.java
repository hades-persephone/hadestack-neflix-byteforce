package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.VideoQuality;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "DTO for returning a user's watch history")
public class WatchHistoryResponse {

    @Schema(description = "Unique identifier of the watch history", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "ID of the movie watched", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID movieId;

    @Schema(description = "ID of the episode watched", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID episodeId;

    @Schema(description = "Timestamp when watched", example = "2025-04-10T10:00:00Z")
    private LocalDateTime watchedAt;

    @Schema(description = "Watch duration in seconds", example = "3600")
    private Integer watchDurationSeconds;

    @Schema(description = "Whether the content was fully watched", example = "false")
    private Boolean completed;

    @Schema(description = "Last position in seconds", example = "1800")
    private Integer lastPositionSeconds;

    @Schema(description = "Device type", example = "Smartphone")
    private String deviceType;

    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Quality watched", example = "HD")
    private VideoQuality qualityWatched;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "Deletion timestamp", example = "null")
    private LocalDateTime deletedAt;

    @Schema(description = "Number of times watched", example = "1")
    private Integer watchCount;

    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
