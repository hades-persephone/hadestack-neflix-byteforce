package io.watch.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingCancelledEvent {
    private UUID ratingId;
    private String reason;
    private String cancelledAt;
}