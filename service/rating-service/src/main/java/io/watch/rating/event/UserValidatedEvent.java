package io.watch.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserValidatedEvent {
    private UUID userId;
    private UUID ratingId;
    private String username;
    private boolean valid;

    public static UserValidatedEvent of(UUID userId, UUID ratingId, String username) {
        return new UserValidatedEvent(userId, ratingId, username, true);
    }
}