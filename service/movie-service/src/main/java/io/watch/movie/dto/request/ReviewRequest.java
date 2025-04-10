package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "DTO for creating/updating a review")
public class ReviewRequest {

    @Schema(description = "ID of the user who wrote this review", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @Schema(description = "ID of the movie being reviewed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID movieId;

    @Schema(description = "ID of the series being reviewed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID seriesId;

    @Schema(description = "ID of the associated rating", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID ratingId;

    @Schema(description = "Review comment", example = "Great movie!")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;

    @Schema(description = "Whether the review contains spoilers", example = "false")
    private Boolean isSpoiler;

    @Schema(description = "Title of the review", example = "Amazing Plot")
    @Size(max = 255, message = "Review title cannot exceed 255 characters")
    private String reviewTitle;

    @Schema(description = "Language of the review", example = "en")
    @Size(max = 10, message = "Language cannot exceed 10 characters")
    private String language;
}
