package io.watch.rating.event;

import lombok.*;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
public class RatingCreatedEvent extends RatingEvent {
    private UUID movieId;
    private UUID userId;
    private UUID aggregateId;
    private Integer ratingValue;
    private String reviewText;
}
