package io.watch.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "departments")
@Schema(description = "Department entity representing a department in the organization")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the department", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "Name of the department", example = "Engineering")
    private String name;

    @Column(name = "description")
    @Schema(description = "Description of the department", example = "Handles software development")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    @Schema(description = "Manager of the department")
    private User manager;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Schema(description = "Users belonging to the department")
    private Set<User> users = new HashSet<>();

    @Column(name = "created_at")
    @CreationTimestamp
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;
}