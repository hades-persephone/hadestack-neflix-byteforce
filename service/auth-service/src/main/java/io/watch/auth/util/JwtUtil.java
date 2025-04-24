package io.watch.auth.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private SecretKey secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(String username, Long userId, List<String> roles, List<String> permissions) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .claim("roles", roles)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("scope", "read write")
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secret, Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractClaims(String token) throws JwtException {
        JwtParser parser = Jwts.parser()
                .verifyWith(secret)
                .build();

        return parser.parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    public String getScopeFromToken(String token) {
        return extractClaims(token).get("scope", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        return extractClaims(token).get("permissions", List.class);
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, String username) {
        try {
            return extractUsername(token).equals(username) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }
}
