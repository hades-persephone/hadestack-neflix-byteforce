package io.watch.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserValidationResponse {
    private String username;
    private String displayName;
    private String email;
    private String errorCode;
    private boolean exists;
    private boolean active;
    private String role;
    private boolean valid;
    private boolean availableForRating;
    private String errorMessage;

    public static Mono<UserValidationResponse> failure(String errorNotFound, String errorCode) {
        return Mono.just(UserValidationResponse.builder()
                .valid(false)
                .errorMessage(errorNotFound)
                .errorCode(errorCode)
                .availableForRating(false)
                .build());
    }

    public static Mono<UserValidationResponse> inactive(String username, String userAccountIsDeactivated) {
        return Mono.just(UserValidationResponse.builder()
                .username(username)
                .errorMessage(userAccountIsDeactivated)
                .build());
    }

    public static Mono<UserValidationResponse> success(String username, boolean exists, String displayName, String email) {
        return Mono.just(UserValidationResponse.builder()
                .username(username)
                .exists(exists)
                .displayName(displayName)
                .email(email)
                .build());
    }
}
