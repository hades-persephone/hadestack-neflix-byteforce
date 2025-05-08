package io.watch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.user.entity.substraction.AccountStatus;
import io.watch.user.entity.substraction.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "Data Transfer Object for User")
public class UserDTO {
    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address of the user", example = "john@example.com")
    private String email;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Gender of the user", example = "MALE")
    private Gender gender;

    @Schema(description = "Department ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID departmentId;

    @Schema(description = "Role IDs", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private Set<UUID> roleIds;

    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;
}