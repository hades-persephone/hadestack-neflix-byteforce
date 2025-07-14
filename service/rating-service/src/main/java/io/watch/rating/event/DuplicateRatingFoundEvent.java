package io.watch.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateRatingFoundEvent {
    private UUID movieId;
    private UUID userId;
    private UUID ratingId;
    private String existingRatingId;
}