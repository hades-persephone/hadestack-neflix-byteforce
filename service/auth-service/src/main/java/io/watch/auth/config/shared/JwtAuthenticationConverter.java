package io.watch.auth.config.shared;

import org.jetbrains.annotations.NotNull;
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

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {

        Collection<SimpleGrantedAuthority> authorities = defaultConverter.convert(jwt)
                .stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority())).collect(Collectors.toList());

        List<String> roles = extractRoles(jwt);
        if (roles != null) {
            roles.forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                authorities.add(new SimpleGrantedAuthority(role));
            });
        }

        List<String> permissions = extractPermissions(jwt);
        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }
        
        return new JwtAuthenticationToken(jwt, authorities);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        return (List<String>) claims.get("roles");
    }

    @SuppressWarnings("unchecked")
    private List<String> extractPermissions(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        return (List<String>) claims.get("permissions");
    }
}