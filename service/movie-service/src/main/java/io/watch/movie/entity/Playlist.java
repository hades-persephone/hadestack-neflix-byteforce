package io.watch.movie.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.ActionType;
import io.watch.movie.entity.substraction.Visibility;
import io.watch.movie.util.ValidEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "playlists")
@Schema(description = "Playlist entity representing a user playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the playlist", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id")
    @Schema(description = "ID of the user owning this playlist", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Column(name = "name", nullable = false)
    @Schema(description = "Name of the playlist", example = "My Favorites")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description")
    @Schema(description = "Description", example = "My favorite movies and series")
    private String description;

    @Column(name = "is_public")
    @Schema(description = "Whether the playlist is public", example = "false")
    private Boolean isPublic = false;

    @Column(name = "created_at")
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    @Schema(description = "Deletion timestamp", example = "null")
    private Instant deletedAt;

    @Size(max = 255, message = "Cover image URL cannot exceed 255 characters")
    @Column(name = "cover_image_url")
    @Schema(description = "Cover image URL", example = "https://example.com/cover.jpg")
    private String coverImageUrl;

    @Min(value = 0, message = "Total items cannot be negative")
    @Column(name = "total_items")
    @Schema(description = "Total items in the playlist", example = "5")
    private Integer totalItems = 0;

    @Column(name = "last_updated")
    @Schema(description = "Last updated timestamp", example = "2025-04-10T12:00:00Z")
    private Instant lastUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    @Schema(description = "Visibility of the playlist", example = "PRIVATE")
    private Visibility visibility = Visibility.PRIVATE;

    @Size(max = 255, message = "Share URL cannot exceed 255 characters")
    @Column(name = "share_url")
    @Schema(description = "Share URL", example = "https://example.com/playlist/123")
    private String shareUrl;

    @Size(max = 50, message = "Playlist type cannot exceed 50 characters")
    @Column(name = "playlist_type")
    @Schema(description = "Type of playlist", example = "Movie")
    private String playlistType;

    @Column(name = "order_number")
    @Schema(description = "Order number", example = "1")
    private Integer orderNumber;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}