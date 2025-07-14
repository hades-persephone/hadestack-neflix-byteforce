package io.watch.rating.client;

import io.watch.rating.dto.UserValidationDto;
import io.watch.rating.dto.UserValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Qualifier("userWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserValidationResponse> validateUser(UUID userId) {
        try {
            log.info("Validating user with ID: {}", userId);

            String url = "http://user-service/api/users/" + userId + "/validate";
            return webClient.get()
                    .uri(url, userId)
                    .retrieve()
                    .bodyToMono(UserValidationDto.class)
                    .map(userDto -> {
                        if (userDto == null || !userDto.isExists()) {
                            return UserValidationResponse.failure("User not found", "USER_NOT_FOUND");
                        }

                        if (!userDto.isActive()) {
                            return UserValidationResponse.inactive(
                                    userDto.getUsername(),
                                    "User account is deactivated"
                            );
                        }

                        return UserValidationResponse.success(
                                userDto.getUsername(),
                                true,
                                userDto.getDisplayName(),
                                userDto.getEmail()
                        );
                    })
                    .doOnError(e -> log.error("Error validating user {}: {}", userId, e.getMessage()))
                    .onErrorReturn(UserValidationResponse.failure(
                            "Validation error", "VALIDATION_ERROR")).block();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found: {}", userId);
            return UserValidationResponse.failure("User not found", "USER_NOT_FOUND");
        } catch (Exception e) {
            log.error("Error validating user {}: {}", userId, e.getMessage());
            return UserValidationResponse.failure(
                    "Validation error: " + e.getMessage(),
                    "VALIDATION_ERROR"
            );
        }
    }

    public Mono<Boolean> checkDuplicateRating(UUID movieId, UUID userId) {
        String url = "/api/users/{userId}/ratings/{movieId}/exists";

        return webClient.get()
                .uri(url, userId, movieId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .defaultIfEmpty(false)
                .doOnError(e -> log.error("Error checking duplicate rating: {}", e.getMessage()))
                .onErrorReturn(false); // Assume no duplicate on error
    }

}
