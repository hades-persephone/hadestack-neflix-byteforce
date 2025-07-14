package io.watch.rating.handler;

import io.watch.rating.client.MovieServiceClient;
import io.watch.rating.client.UserServiceClient;
import io.watch.rating.command.*;
import io.watch.rating.dto.MovieValidationResponse;
import io.watch.rating.dto.UserValidationResponse;
import io.watch.rating.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Aggregate
@Component
public class ValidationCommandHandler {

    @AggregateIdentifier
    private String aggregateId;
    private final MovieServiceClient movieServiceClient;
    private final UserServiceClient userServiceClient;
    private final EventGateway eventGateway;

    @CommandHandler
    public void handle(ValidateMovieCommand command) {
        log.info("Handling ValidateMovieCommand for movieId: {}", command.getMovieId());

        MovieValidationResponse response = movieServiceClient.validateMovie(command.getMovieId()).block();

        if (response.isValid()) {
            eventGateway.publish(null, new MovieValidatedEvent(
                    command.getMovieId(),
                    command.getRatingId(),
                    response.getTitle(),
                    true
            ));
        } else {
            eventGateway.publish(null, new MovieValidationFailedEvent(
                    command.getMovieId(),
                    command.getRatingId(),
                    response.getErrorMessage()
            ));
        }
    }

    @CommandHandler
    public void handle(ValidateUserCommand command) {
        log.info("Handling ValidateUserCommand for userId: {}", command.getUserId());

        UserValidationResponse response = userServiceClient.validateUser(command.getUserId()).block();

        if (response.isValid() && response.isActive()) {
            eventGateway.publish(null, new UserValidatedEvent(
                    command.getUserId(),
                    command.getRatingId(),
                    response.getUsername(),
                    true
            ));
        } else {
            eventGateway.publish(null, new UserValidationFailedEvent(
                    command.getUserId(),
                    command.getRatingId(),
                    response.getErrorMessage()
            ));
        }
    }

    @CommandHandler
    public void handle(CheckDuplicateRatingCommand command) {
        log.info("Handling CheckDuplicateRatingCommand for movieId: {} and userId: {}",
                command.getMovieId(), command.getUserId());

        boolean duplicateExists = Boolean.TRUE.equals(userServiceClient.checkDuplicateRating(
                command.getMovieId(), command.getUserId()).block());

        if (!duplicateExists) {
            eventGateway.publish(null, new NoDuplicateRatingEvent(
                    command.getMovieId(),
                    command.getUserId(),
                    command.getRatingId()
            ));
        } else {
            eventGateway.publish(null, new DuplicateRatingFoundEvent(
                    command.getMovieId(),
                    command.getUserId(),
                    command.getRatingId(),
                    "existing-rating-id"
            ));
        }
    }

    @CommandHandler
    public void handle(FinalizeRatingCommand command) {
        log.info("Handling FinalizeRatingCommand for ratingId: {}", command.getRatingId());

        eventGateway.publish(null, new RatingFinalizedEvent(
                command.getRatingId(),
                command.getMovieId(),
                command.getUserId(),
                command.getRatingValue(),
                command.getReviewText(),
                "COMPLETED"
        ));
    }

    @CommandHandler
    public void handle(CancelRatingCommand command) {
        log.info("Handling CancelRatingCommand for ratingId: {} with reason: {}",
                command.getRatingId(), command.getReason());

        eventGateway.publish(null, new RatingCancelledEvent(
                command.getRatingId(),
                command.getReason(),
                java.time.Instant.now().toString()
        ));
    }

    @EventSourcingHandler
    public void on(MovieValidatedEvent event) {
        log.info("Movie validated event processed for movieId: {}", event.getMovieId());
    }

    @EventSourcingHandler
    public void on(UserValidatedEvent event) {
        log.info("User validated event processed for userId: {}", event.getUserId());
    }

    @EventSourcingHandler
    public void on(NoDuplicateRatingEvent event) {
        log.info("No duplicate rating event processed for movieId: {} and userId: {}",
                event.getMovieId(), event.getUserId());
    }

    @EventSourcingHandler
    public void on(RatingFinalizedEvent event) {
        log.info("Rating finalized event processed for ratingId: {}", event.getRatingId());
    }

    @EventSourcingHandler
    public void on(RatingCancelledEvent event) {
        log.info("Rating cancelled event processed for ratingId: {}", event.getRatingId());
    }
}
