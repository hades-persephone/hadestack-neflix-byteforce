package io.watch.rating.handler;

import io.watch.rating.client.MovieServiceClient;
import io.watch.rating.client.UserServiceClient;
import io.watch.rating.command.*;
import io.watch.rating.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import java.util.UUID;

@Saga
@RequiredArgsConstructor
@Slf4j
public class RatingSaga {

    private transient CommandGateway commandGateway;
    private transient MovieServiceClient movieServiceClient;
    private transient UserServiceClient userServiceClient;

    private UUID ratingId;
    private UUID movieId;
    private UUID userId;
    private Integer ratingValue;
    private String reviewText;

    @StartSaga
    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(RatingCreatedEvent event) {
        log.info("Starting rating creation saga for event: {}", event.getEventId());

        this.ratingId = event.getAggregateId();
        this.movieId = event.getMovieId();
        this.userId = event.getUserId();
        this.ratingValue = event.getRatingValue();
        this.reviewText = event.getReviewText();

        // Step 1: Validate movie exists
        commandGateway.send(new ValidateMovieCommand(movieId, ratingId), null);
    }

    @SagaEventHandler(associationProperty = "")
    public void handle(MovieValidatedEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.info("Movie validated for rating: {}", ratingId);

            // Step 2: Validate user exists and has permission
            commandGateway.send(new ValidateUserCommand(userId, ratingId), null);
        }
    }

    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(UserValidatedEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.info("User validated for rating: {}", ratingId);

            // Step 3: Check if user has already rated this movie
            commandGateway.send(new CheckDuplicateRatingCommand(movieId, userId, ratingId), null);
        }
    }

    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(NoDuplicateRatingEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.info("No duplicate rating found for rating: {}", ratingId);

            // Step 4: Finalize rating creation
            commandGateway.send(new FinalizeRatingCommand(ratingId, movieId, userId, ratingValue, reviewText), null);
        }
    }

    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(RatingFinalizedEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.info("Rating creation saga completed successfully for: {}", ratingId);
            // Saga completed successfully
        }
    }

    // Compensation handlers for rollback
    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(MovieValidationFailedEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.warn("Movie validation failed for rating: {}", ratingId);
            commandGateway.send(new CancelRatingCommand(ratingId, "Movie validation failed"), null);
        }
    }

    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(UserValidationFailedEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.warn("User validation failed for rating: {}", ratingId);
            commandGateway.send(new CancelRatingCommand(ratingId, "User validation failed"), null);
        }
    }

    @SagaEventHandler(associationProperty = "ratingId")
    public void handle(DuplicateRatingFoundEvent event) {
        if (event.getRatingId().equals(this.ratingId)) {
            log.warn("Duplicate rating found for rating: {}", ratingId);
            commandGateway.send(new CancelRatingCommand(ratingId, "Duplicate rating found"), null);
        }
    }

}
