package io.watch.auth.config;

import io.watch.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JwtService jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);
                List<String> permissions = jwtUtil.getPermissionsFromToken(token);
                String scope = jwtUtil.getScopeFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    // Add role-based authorities with ROLE_ prefix
                    if (roles != null) {
                        roles.forEach(role -> {
                            // Add both with and without ROLE_ prefix for compatibility
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            authorities.add(new SimpleGrantedAuthority(role));
                        });
                    }

                    // Add permission-based authorities
                    if (permissions != null) {
                        authorities.addAll(permissions.stream().map(SimpleGrantedAuthority::new).toList());
                    }

                    // Add scope-based authorities
                    if (scope != null) {
                        authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                    }

                    logger.debug("User {} authenticated with roles: {} and permissions: {}", username, roles, permissions);

                    // Create principal with roles and permissions
                    CustomUserPrincipal principal = new CustomUserPrincipal(username, userId, authorities, roles, permissions);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("Authentication error: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
