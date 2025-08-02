package io.watch.auth.service;

import java.util.Map;
import java.util.Optional;

/**
 * Service interface for Keycloak authentication operations.
 */
public interface KeycloakAuthenticationService {
    
    /**
     * Authenticate a user with username and password.
     *
     * @param username the username
     * @param password the password
     * @return an Optional containing the access token if authentication is successful, empty otherwise
     */
    Optional<String> authenticate(String username, String password);
    
    /**
     * Get user information from Keycloak.
     *
     * @param token the access token
     * @return a Map containing user information if successful, empty otherwise
     */
    Optional<Map<String, Object>> getUserInfo(String token);
    
    /**
     * Validate a token with Keycloak.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}