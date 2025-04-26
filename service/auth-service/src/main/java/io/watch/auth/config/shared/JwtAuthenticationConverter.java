package io.watch.auth.config.shared;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converter for extracting authorities from a JWT token.
 * This converter extracts roles and permissions from the JWT token and adds them as authorities.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Convert a JWT token to an authentication token with authorities.
     *
     * @param jwt the JWT token
     * @return the authentication token with authorities
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Add default authorities (scopes)
        authorities.addAll(defaultConverter.convert(jwt)
                .stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .collect(Collectors.toList()));
        
        // Extract roles from the JWT token
        List<String> roles = extractRoles(jwt);
        if (roles != null) {
            roles.forEach(role -> {
                // Add both with and without ROLE_ prefix for compatibility
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                authorities.add(new SimpleGrantedAuthority(role));
            });
        }
        
        // Extract permissions from the JWT token
        List<String> permissions = extractPermissions(jwt);
        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }
        
        return new JwtAuthenticationToken(jwt, authorities);
    }
    
    /**
     * Extract roles from the JWT token.
     *
     * @param jwt the JWT token
     * @return the list of roles
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        return (List<String>) claims.get("roles");
    }
    
    /**
     * Extract permissions from the JWT token.
     *
     * @param jwt the JWT token
     * @return the list of permissions
     */
    @SuppressWarnings("unchecked")
    private List<String> extractPermissions(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        return (List<String>) claims.get("permissions");
    }
}