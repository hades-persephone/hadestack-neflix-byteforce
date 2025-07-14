package io.watch.rating.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RatingUpdatedEvent extends RatingEvent {
    private UUID ratingId;
    private UUID userId;
    private UUID movieId;
    private Integer oldRating;
    private Integer newRating;
    private String oldReview;
    private String newReview;
    private LocalDateTime timestamp;
}
