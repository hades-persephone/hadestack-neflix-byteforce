package io.watch.rating.client;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.watch.rating.dto.UserValidationDto;
import io.watch.rating.dto.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;
    private final Retry userValidationRetry;
    private final Retry ratingCheckRetry;

    public UserServiceClient(@Qualifier("userWebClient") WebClient webClient, RetryRegistry retryRegistry) {
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
        this.userValidationRetry = retryRegistry.retry("userValidation", userValidationConfig);

        RetryConfig ratingCheckConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(
                        HttpClientErrorException.class,
                        WebClientResponseException.InternalServerError.class,
                        WebClientResponseException.BadGateway.class,
                        WebClientResponseException.ServiceUnavailable.class,
                        WebClientResponseException.GatewayTimeout.class)
                .ignoreExceptions(
                        WebClientResponseException.NotFound.class,
                        WebClientResponseException.BadRequest.class
                )
                .build();
        this.ratingCheckRetry = retryRegistry.retry("ratingCheck", ratingCheckConfig);


        userValidationRetry.getEventPublisher()
                .onRetry(event -> log.warn("User validation retry attempt {} for user validation",
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("User validation failed after {} attempts: {}",
                        event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));

        ratingCheckRetry.getEventPublisher()
                .onRetry(event -> log.warn("Rating check retry attempt {} for rating check",
                        event.getNumberOfRetryAttempts()))
                .onError(event -> log.error("Rating check failed after {} attempts: {}",
                        event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));
    }

    public Mono<UserValidationResponse> validateUser(UUID userId) {
        log.info("Validating user with ID: {}", userId);

        String url = "http://user-service/api/users/" + userId + "/validate";

        return webClient.get()
                    .uri(url, userId)
                    .retrieve()
                    .bodyToMono(UserValidationDto.class)
                    .map(userDto -> UserValidationResponse.success(
                            userDto.getUsername(),
                            true,
                            userDto.getDisplayName(),
                            userDto.getEmail()
                    ))
                    .transformDeferred(RetryOperator.of(userValidationRetry))
                    .doOnError(e -> log.error("Error validating user {}: {}", userId, e.getMessage()))
                    .onErrorResume(WebClientResponseException.NotFound.class,
                    e -> {
                                log.warn("User not found: {}", userId);
                                return Mono.just(UserValidationResponse.failure("User not found", "USER_NOT_FOUND"));
                            })
                    .onErrorResume(WebClientResponseException.class,
                            e -> {
                                log.error("WebClient error validating user {}: {} - {}",
                                        userId, e.getStatusCode(), e.getMessage());
                                return Mono.just(UserValidationResponse.failure("User not found", "USER_NOT_FOUND"));
                            })
                    .onErrorResume(Exception.class,
                            e -> {
                                log.error("Unexpected error validating user {}: {}", userId, e.getMessage());
                                return Mono.just(UserValidationResponse.failure(
                                        "Validation error: " + e.getMessage(), "VALIDATION_ERROR"));
        }).block();
    }

    public Mono<Boolean> checkDuplicateRating(UUID movieId, UUID userId) {
        String url = "/api/users/{userId}/ratings/{movieId}/exists";

        return webClient.get()
                .uri(url, userId, movieId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .defaultIfEmpty(false)
                .transformDeferred(RetryOperator.of(ratingCheckRetry))
                .doOnError(e -> log.error("Error checking duplicate rating for movie {} and user {}: {}",
                        movieId, userId, e.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class,
                        e -> {
                            log.debug("Rating not found for movie {} and user {}", movieId, userId);
                            return Mono.just(false);
                        })
                .onErrorReturn(false);
    }

}
