package io.watch.auth.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserPrincipal {
    private final String username;
    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final List<String> roles;
    private final List<String> permissions;

    public CustomUserPrincipal(String username, Long userId, Collection<? extends GrantedAuthority> authorities,
                              List<String> roles, List<String> permissions) {
        this.username = username;
        this.userId = userId;
        this.authorities = authorities;
        this.roles = roles;
        this.permissions = permissions;
    }

    public boolean hasRole(String roleName) {
        return roles.contains(roleName) ||
               authorities.stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    public boolean hasPermission(String permissionName) {
        return permissions.contains(permissionName) ||
               authorities.stream()
                        .anyMatch(a -> a.getAuthority().equals(permissionName));
    }
}
