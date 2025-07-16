package io.watch.rating.client;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.watch.rating.dto.MovieValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class MovieServiceClient {

    private final WebClient webClient;
    private final Retry movieValidationRetry;

    public MovieServiceClient(@Qualifier("movieWebClient") WebClient webClient, RetryRegistry retryRegistry) {
        this.webClient = webClient;

        RetryConfig userValidationConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(
                        HttpClientErrorException.class,
                        WebClientResponseException.InternalServerError.class,
                        WebClientResponseException.BadGateway.class,
                        WebClientResponseException.ServiceUnavailable.class,
                        WebClientResponseException.GatewayTimeout.class
                )
                .ignoreExceptions(
                        WebClientResponseException.NotFound.class,
                        WebClientResponseException.BadRequest.class,
                        WebClientResponseException.Unauthorized.class,
                        WebClientResponseException.Forbidden.class
                )
                .build();
        this.movieValidationRetry = retryRegistry.retry("movieValidation", userValidationConfig);

        movieValidationRetry.getEventPublisher()
                .onRetry(event -> log.warn("Movie validation retry attempt {} for movie validation",
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("Movie validation failed after {} attempts: {}",
                        event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));
    }

    public Mono<MovieValidationResponse> validateMovie(UUID movieId) {
        String url = "/api/movies/" + movieId + "/validate";

        log.info("Validating movie with ID: {} at URL: {}", movieId, url);

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(MovieValidationResponse.class)
                .doOnNext(response -> log.info("Movie validation response received for ID: {}", movieId))
                .transformDeferred(RetryOperator.of(movieValidationRetry))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.warn("Movie service returned error: {}", e.getMessage());
                    return Mono.just(MovieValidationResponse.builder()
                            .movieId(movieId)
                            .isValid(false)
                            .errorMessage("Movie service error: " + e.getMessage())
                            .build());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error validating movie {}: {}", movieId, e.getMessage());
                    return Mono.just(MovieValidationResponse.builder()
                            .movieId(movieId)
                            .isValid(false)
                            .errorMessage("Unexpected error: " + e.getMessage())
                            .build());
                });
    }
}
