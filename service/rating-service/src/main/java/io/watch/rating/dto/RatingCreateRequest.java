package io.watch.rating.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RatingCreateRequest {
    @NotBlank(message = "User ID is required")
    private UUID userId;
    @NotBlank(message = "Product ID is required")
    private UUID movieId;
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 2000, message = "Review must not exceed 2000 characters")
    private String review;

    @Valid
    private RatingMetadata metadata;

}
