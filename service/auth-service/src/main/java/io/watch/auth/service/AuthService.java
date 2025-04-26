package io.watch.auth.service;

import io.watch.auth.dto.AuthResponse;
import io.watch.auth.dto.LoginRequest;
import io.watch.auth.dto.RefreshTokenRequest;
import io.watch.auth.dto.RegisterRequest;
import reactor.core.publisher.Mono;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {
    
    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param loginRequest the login request containing username and password
     * @return an AuthResponse containing the JWT token and user details
     */
    Mono<AuthResponse> login(LoginRequest loginRequest);
    /**
     * Register a new user and generate a JWT token.
     *
     * @param registerRequest the registration request containing user details
     * @return an AuthResponse containing the JWT token and user details
     */
    AuthResponse register(RegisterRequest registerRequest);

    Mono<AuthResponse> refreshToken(RefreshTokenRequest request);

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
    
    /**
     * Get the current authenticated user.
     *
     * @return an AuthResponse containing the user details
     */
    AuthResponse getCurrentUser();
}