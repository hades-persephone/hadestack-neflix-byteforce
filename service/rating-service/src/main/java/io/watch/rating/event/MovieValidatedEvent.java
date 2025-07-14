package io.watch.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieValidatedEvent {
    private UUID movieId;
    private UUID ratingId;
    private String movieTitle;
    private boolean isValid;
}