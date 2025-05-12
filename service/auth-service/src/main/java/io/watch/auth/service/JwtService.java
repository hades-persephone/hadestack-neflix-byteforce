package io.watch.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.watch.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public String generateToken(String username, UUID userId, List<String> roles, List<String> permissions) {
        return getClaimsMap(username, userId, roles, permissions, accessTokenExpiration);
    }

    public String generateRefreshToken(String username, UUID userId, List<String> roles, List<String> permissions) {
        return getClaimsMap(username, userId, roles, permissions, refreshTokenExpiration);
    }


    private String getClaimsMap(String username, UUID userId, List<String> roles, List<String> permissions, Long tokenExpiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("expires_in", tokenExpiration);
        claims.put("scope", permissions.stream()
                .map(p -> p.substring(p.indexOf(":") + 1))
                .collect(Collectors.toSet()));
        return createToken(claims, username, tokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
//                .signWith(SignatureAlgorithm.HS512, getSignInKey())
                .compact();
    }

    public Claims extractClaims(String token) throws JwtException {
        JwtParser parser = Jwts.parser()
                .verifyWith((SecretKey) getSignInKey())
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

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(JwtUtil.hexToBytes(secret));
    }
}
