package io.watch.rating.dto;

import io.watch.rating.entity.RatingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RatingResponse {
    private UUID id;
    private UUID userId;
    private UUID movieId;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
    private Boolean isVerified;
    private RatingStatus status;
}
