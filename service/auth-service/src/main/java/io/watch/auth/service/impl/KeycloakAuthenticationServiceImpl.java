package io.watch.auth.service.impl;

import io.watch.auth.client.KeycloakClient;
import io.watch.auth.service.KeycloakAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the KeycloakAuthenticationService interface.
 * Uses Keycloak for authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthenticationServiceImpl implements KeycloakAuthenticationService {

    private final KeycloakClient keycloakClient;

    @Override
    public Optional<String> authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);
        return keycloakClient.authenticate(username, password);
    }

    @Override
    public Optional<Map<String, Object>> getUserInfo(String token) {
        log.debug("Getting user info for token");
        return keycloakClient.getUserInfo(token);
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("Validating token");
        return keycloakClient.validateToken(token);
    }
}