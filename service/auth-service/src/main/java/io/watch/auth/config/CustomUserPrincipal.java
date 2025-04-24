package io.watch.auth.config;

import lombok.Getter;

@Getter
public class CustomUserPrincipal {
    private final String username;
    private final Long userId;

    public CustomUserPrincipal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }
}
