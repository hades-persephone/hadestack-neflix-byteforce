package io.watch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Data Transfer Object for Department")
public class DepartmentDTO {
    @Schema(description = "Unique identifier of the department", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Name of the department", example = "Engineering")
    private String name;

    @Schema(description = "Description of the department", example = "Handles software development")
    private String description;

    @Schema(description = "Manager ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID managerId;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;
}