package io.watch.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the authentication system.
 * This entity is used for authentication and authorization.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Add a role to the user.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Remove a role from the user.
     *
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Check if the user has a specific role.
     *
     * @param roleName the name of the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Check if the user has a specific permission.
     *
     * @param permissionName the name of the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String permissionName) {
        return this.roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
}
