package io.watch.auth.dto;

import lombok.*;

import java.util.List;

/**
 * DTO for authentication responses.
 */
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private List<String> roles;
    private List<String> permissions;

    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }

    public boolean hasPermission(String permissionName) {
        return permissions != null && permissions.contains(permissionName);
    }
}
