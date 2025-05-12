package io.watch.auth.repository;

import io.watch.auth.entity.RefreshToken;
import io.watch.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing RefreshToken entities.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    /**
     * Find a refresh token by its token value.
     *
     * @param token the token value to search for
     * @return an Optional containing the refresh token if found, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Find all refresh tokens for a user.
     *
     * @param user the user to search for
     * @return a list of refresh tokens for the user
     */
    List<RefreshToken> findByUser(User user);
    
    /**
     * Delete all refresh tokens for a user.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);
    
    /**
     * Delete all expired tokens.
     *
     * @param now the current time
     * @return the number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < ?1")
    int deleteAllExpiredTokens(Instant now);
    
    /**
     * Find all valid tokens for a user (not expired and not revoked).
     *
     * @param user the user to search for
     * @param now the current time
     * @return a list of valid refresh tokens for the user
     */
    @Query("SELECT t FROM RefreshToken t WHERE t.user = ?1 AND t.expiryDate >= ?2 AND t.revoked = false")
    List<RefreshToken> findValidTokensByUser(User user, Instant now);
    
    /**
     * Check if a token exists and is valid.
     *
     * @param token the token value to check
     * @param now the current time
     * @return true if the token exists, is not expired, and is not revoked
     */
    @Query("SELECT COUNT(t) > 0 FROM RefreshToken t WHERE t.token = ?1 AND t.expiryDate >= ?2 AND t.revoked = false")
    boolean existsByTokenAndValid(String token, Instant now);
}