package io.watch.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for authentication responses.
 */
@Data
@Builder
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

    /**
     * Check if the user has a specific role.
     *
     * @param roleName the name of the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }

    /**
     * Check if the user has a specific permission.
     *
     * @param permissionName the name of the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String permissionName) {
        return permissions != null && permissions.contains(permissionName);
    }
}
