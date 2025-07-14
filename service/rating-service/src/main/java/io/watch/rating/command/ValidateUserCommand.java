package io.watch.rating.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private UUID ratingId;

    public static ValidateUserCommand of(UUID userId, UUID ratingId) {
        return new ValidateUserCommand(userId, ratingId);
    }
}
