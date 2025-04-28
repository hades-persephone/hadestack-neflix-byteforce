package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Data
public abstract class EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(name = "is_available")
    @Schema(description  = "Availability flag", example = "true")
    private Boolean isAvailable = true;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @UpdateTimestamp
    private LocalDateTime deletedAt;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;

}
