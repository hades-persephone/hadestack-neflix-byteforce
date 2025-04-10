package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "DTO for returning review data")
public class ReviewResponse {

    @Schema(description = "Unique identifier of the review", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID of the user who wrote this review", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "ID of the movie being reviewed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID movieId;

    @Schema(description = "ID of the series being reviewed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID seriesId;

    @Schema(description = "ID of the associated rating", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID ratingId;

    @Schema(description = "Review comment", example = "Great movie!")
    private String comment;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private Instant updatedAt;

    @Schema(description = "Deletion timestamp", example = "null")
    private Instant deletedAt;

    @Schema(description = "Number of likes", example = "10")
    private Integer likesCount;

    @Schema(description = "Number of dislikes", example = "2")
    private Integer dislikesCount;

    @Schema(description = "Whether the review contains spoilers", example = "false")
    private Boolean isSpoiler;

    @Schema(description = "Title of the review", example = "Amazing Plot")
    private String reviewTitle;

    @Schema(description = "Language of the review", example = "en")
    private String language;

    @Schema(description = "Visibility of the review", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
