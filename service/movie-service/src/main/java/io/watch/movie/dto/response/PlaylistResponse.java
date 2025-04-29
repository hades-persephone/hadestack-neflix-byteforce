package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for playlist")
public class PlaylistResponse {

    @Schema(description = "Unique identifier of the playlist", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    private UUID userId;
    private String name;
    private String description;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String coverImageUrl;
    private Integer totalItems;
    private LocalDateTime lastUpdated;
    private String visibility;
    private String shareUrl;
    private String playlistType;
    private Integer orderNumber;
    private UUID createdBy;
    private UUID updatedBy;
}
