package io.watch.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "permissions")
@Schema(description = "Permission entity representing a specific permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the permission", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "Name of the permission", example = "READ_USER")
    private String name;

    @Column(name = "description")
    @Schema(description = "Description of the permission", example = "Permission to read user data")
    private String description;

    @Column(name = "created_at")
    @CreationTimestamp
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;
}