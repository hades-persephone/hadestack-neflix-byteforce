package io.watch.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Entity representing a refresh token.
 * Refresh tokens are used to obtain new access tokens without requiring the user to re-authenticate.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "revoked")
    private boolean revoked = false;

    /**
     * Check if the refresh token is expired.
     *
     * @return true if the token is expired, false otherwise
     */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the refresh token is valid (not expired and not revoked).
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }
}