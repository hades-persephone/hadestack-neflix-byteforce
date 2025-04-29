package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a playlist")
public class PlaylistRequest {

    @Schema(description = "Request playlist ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID of the user who owns the playlist", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @Schema(description = "Name of the playlist", example = "My Favorites")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @Schema(description = "Description of the playlist", example = "My favorite movies and series")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Whether the playlist is public", example = "true")
    private Boolean isPublic;

    @Schema(description = "Cover image URL", example = "https://example.com/cover.jpg")
    @Size(max = 255, message = "Cover image URL cannot exceed 255 characters")
    private String coverImageUrl;

    @Schema(description = "Total items in the playlist", example = "5")
    @Min(value = 0, message = "Total items cannot be negative")
    private Integer totalItems;

    @Schema(description = "Visibility of the playlist", example = "PRIVATE")
    private String visibility;

    @Schema(description = "Share URL", example = "https://example.com/playlist/123")
    @Size(max = 255, message = "Share URL cannot exceed 255 characters")
    private String shareUrl;

    @Schema(description = "Type of playlist", example = "Movie")
    @Size(max = 50, message = "Playlist type cannot exceed 50 characters")
    private String playlistType;

    @Schema(description = "Order number", example = "1")
    private Integer orderNumber;
}
