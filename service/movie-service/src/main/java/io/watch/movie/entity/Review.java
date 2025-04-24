package io.watch.movie.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.Visibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Data
@Entity
@Table(name = "reviews")
@Schema(description = "Review entity representing a user review")
public class Review extends EntityBase {

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id")
    @Schema(description = "ID of the user who wrote this review", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Column(name = "movie_id")
    @Schema(description = "ID of the movie being reviewed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID movieId;

    @Column(name = "series_id")
    @Schema(description = "ID of the series being reviewed", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID seriesId;

    @Column(name = "rating_id")
    @Schema(description = "ID of the associated rating", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID ratingId;

    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    @Column(name = "comment")
    @Schema(description = "Review comment", example = "Great movie!")
    private String comment;

    @Min(value = 0, message = "Likes count cannot be negative")
    @Column(name = "likes_count")
    @Schema(description = "Number of likes", example = "10")
    private Integer likesCount = 0;

    @Min(value = 0, message = "Dislikes count cannot be negative")
    @Column(name = "dislikes_count")
    @Schema(description = "Number of dislikes", example = "2")
    private Integer dislikesCount = 0;

    @Column(name = "is_spoiler")
    @Schema(description = "Whether the review contains spoilers", example = "false")
    private Boolean isSpoiler = false;

    @Size(max = 255, message = "Review title cannot exceed 255 characters")
    @Column(name = "review_title")
    @Schema(description = "Title of the review", example = "Amazing Plot")
    private String reviewTitle;

    @Size(max = 10, message = "Language cannot exceed 10 characters")
    @Column(name = "language")
    @Schema(description = "Language of the review", example = "en")
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    @Schema(description = "Visibility of the review", example = "PUBLIC")
    private Visibility visibility = Visibility.PUBLIC;

}
