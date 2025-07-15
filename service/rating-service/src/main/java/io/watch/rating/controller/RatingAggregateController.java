package io.watch.rating.controller;

import io.watch.rating.command.CancelRatingCommand;
import io.watch.rating.command.FinalizeRatingCommand;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.service.RatingAggregateService;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/ratings/aggregate")
@RequiredArgsConstructor
public class RatingAggregateController {

    private final RatingAggregateService ratingAggregateService;
    private final CommandGateway commandGateway;

    @GetMapping("/statistics/{movieId}")
    public ResponseEntity<RatingStatistics> getMovieRatingStatistics(@PathVariable UUID movieId) {
        RatingStatistics statistics = ratingAggregateService.getMovieRatingStatistics(movieId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/average/{movieId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID movieId) {
        Double averageRating = ratingAggregateService.getAverageRating(movieId);
        return ResponseEntity.ok(averageRating);
    }

    @PostMapping("/finalize/{ratingId}")
    public ResponseEntity<Void> finalizeRating(
            @PathVariable UUID ratingId,
            @RequestParam UUID movieId,
            @RequestParam UUID userId,
            @RequestParam Integer ratingValue,
            @RequestParam(required = false) String reviewText) {

        FinalizeRatingCommand command = new FinalizeRatingCommand(
                ratingId, movieId, userId, ratingValue, reviewText);

        CompletableFuture<Void> result = commandGateway.send(command);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/cancel/{ratingId}")
    public ResponseEntity<Void> cancelRating(
            @PathVariable UUID ratingId,
            @RequestParam String reason) {

        CancelRatingCommand command = new CancelRatingCommand(ratingId, reason);

        CompletableFuture<Void> result = commandGateway.send(command);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/recalculate/{movieId}")
    public ResponseEntity<Void> recalculateStatistics(@PathVariable UUID movieId) {
        ratingAggregateService.recalculateStatistics(movieId);
        return ResponseEntity.accepted().build();
    }
}
