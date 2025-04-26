package io.watch.auth.service;

import io.watch.auth.entity.RefreshToken;
import io.watch.auth.entity.User;

import java.util.Optional;

/**
 * Service interface for refresh token operations.
 */
public interface RefreshTokenService {
    
    /**
     * Create a new refresh token for a user.
     *
     * @param userId the ID of the user
     * @return the created refresh token
     */
    RefreshToken createRefreshToken(Long userId);
    
    /**
     * Verify if a refresh token is valid.
     *
     * @param token the refresh token to verify
     * @return the refresh token if valid
     * @throws RuntimeException if the token is invalid or expired
     */
    RefreshToken verifyExpiration(String token);
    
    /**
     * Find a refresh token by its token value.
     *
     * @param token the token value to search for
     * @return an Optional containing the refresh token if found, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Delete all refresh tokens for a user.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);
    
    /**
     * Revoke a refresh token.
     *
     * @param token the token to revoke
     */
    void revokeRefreshToken(String token);
    
    /**
     * Delete all expired tokens.
     */
    void deleteExpiredTokens();
}