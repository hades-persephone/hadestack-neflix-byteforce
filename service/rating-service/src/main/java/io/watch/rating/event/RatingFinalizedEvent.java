package io.watch.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingFinalizedEvent {
    private UUID ratingId;
    private UUID movieId;
    private UUID userId;
    private Integer ratingValue;
    private String reviewText;
    private String status;
}