package io.watch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "Data Transfer Object for Role")
public class RoleDTO {
    @Schema(description = "Unique identifier of the role", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    @Schema(description = "Name of the role", example = "ADMIN")
    private String name;

    @Schema(description = "Description of the role", example = "Administrator role")
    private String description;

    @Schema(description = "Permission IDs", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private Set<UUID> permissionIds;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;
}