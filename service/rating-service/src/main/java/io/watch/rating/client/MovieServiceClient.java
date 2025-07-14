package io.watch.rating.client;

import io.watch.rating.dto.MovieValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class MovieServiceClient {

    private final WebClient webClient;

    public MovieServiceClient(@Qualifier("movieWebClient") WebClient webClient) {
        this.webClient = webClient;
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
