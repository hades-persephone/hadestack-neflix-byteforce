package io.watch.rating.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRatingCommand {
    @TargetAggregateIdentifier
    private UUID ratingId;
    private String reason;
}
