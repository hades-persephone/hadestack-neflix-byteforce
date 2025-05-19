package io.watch.user.dto;

import io.watch.user.entity.Department;
import io.watch.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private boolean enabled;
    private String accountStatus;
    private String preferredLanguage;
    private Department department;
    private Set<Role> roles;
    private List<String> permissions;
}
