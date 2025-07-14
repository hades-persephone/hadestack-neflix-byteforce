package io.watch.rating.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserValidationFailedEvent {
    private UUID userId;
    private UUID ratingId;
    private String errorMessage;

    public static UserValidationFailedEvent of(UUID userId, UUID ratingId, String errorMessage) {
        return new UserValidationFailedEvent(userId, ratingId, errorMessage);
    }
}