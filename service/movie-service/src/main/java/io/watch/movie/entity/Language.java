package io.watch.movie.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.ActionType;
import io.watch.movie.util.ValidEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "languages")
@Schema(description = "Language entity representing a language")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the language", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "Name of the language", example = "English")
    private String name;

    @NotBlank(message = "Code cannot be blank")
    @Size(min = 2, max = 10, message = "Code must be between 2 and 10 characters")
    @Column(name = "code", nullable = false)
    @Schema(description = "Language code", example = "en")
    private String code;

    @Column(name = "is_active")
    @Schema(description = "Whether the language is active", example = "true")
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    @Schema(description = "Deletion timestamp", example = "null")
    private Instant deletedAt;

    @Size(max = 50, message = "Region cannot exceed 50 characters")
    @Column(name = "region")
    @Schema(description = "Region", example = "North America")
    private String region;

    @Size(max = 50, message = "Native name cannot exceed 50 characters")
    @Column(name = "native_name")
    @Schema(description = "Native name", example = "English")
    private String nativeName;

    @Min(value = 0, message = "Popularity score cannot be negative")
    @Column(name = "popularity_score")
    @Schema(description = "Popularity score", example = "100")
    private Integer popularityScore;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}