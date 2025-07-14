package io.watch.rating.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RatingDeletedEvent extends RatingEvent {
    private UUID ratingId;
    private UUID userId;
    private UUID movieId;
    private LocalDateTime timestamp;
    private String reason;
}
