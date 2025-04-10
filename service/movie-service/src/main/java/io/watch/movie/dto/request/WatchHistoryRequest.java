package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.VideoQuality;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "DTO for creating or updating a user's watch history")
public class WatchHistoryRequest {

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "ID of the movie watched", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID movieId;

    @Schema(description = "ID of the episode watched", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID episodeId;

    @Min(value = 0, message = "Watch duration cannot be negative")
    @Schema(description = "Watch duration in seconds", example = "3600")
    private Integer watchDurationSeconds;

    @Schema(description = "Whether the content was fully watched", example = "false")
    private Boolean completed;

    @Min(value = 0, message = "Last position cannot be negative")
    @Schema(description = "Last position in seconds", example = "1800")
    private Integer lastPositionSeconds;

    @Size(max = 50, message = "Device type cannot exceed 50 characters")
    @Schema(description = "Device type", example = "Smartphone")
    private String deviceType;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Quality watched", example = "HD")
    private VideoQuality qualityWatched;

    @Min(value = 1, message = "Watch count must be at least 1")
    @Schema(description = "Number of times watched", example = "1")
    private Integer watchCount;

    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
