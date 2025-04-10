package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "directors")
@Schema(description = "Director entity representing a director")
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the director", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    @Schema(description = "Full name of the director", example = "Christopher Nolan")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    @Schema(description = "Date of birth", example = "1970-07-30")
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    @Column(name = "nationality")
    @Schema(description = "Nationality", example = "UK")
    private String nationality;

    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    @Column(name = "biography")
    @Schema(description = "Biography", example = "A visionary director known for...")
    private String biography;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    @Column(name = "profile_picture_url")
    @Schema(description = "Profile picture URL", example = "https://example.com/nolan.jpg")
    private String profilePictureUrl;

    @Column(name = "is_active")
    @Schema(description = "Whether the director is active", example = "true")
    private Boolean isActive = true;

    @Column(name = "created_at")
    @CreationTimestamp
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @UpdateTimestamp
    @Schema(description = "Deletion timestamp", example = "null")
    private LocalDateTime deletedAt;

    @Size(max = 255, message = "IMDb profile URL cannot exceed 255 characters")
    @Column(name = "imdb_profile_url")
    @Schema(description = "IMDb profile URL", example = "https://imdb.com/name/nm0634240")
    private String imdbProfileUrl;

    @Size(max = 2000, message = "Awards cannot exceed 2000 characters")
    @Column(name = "awards")
    @Schema(description = "Awards won", example = "BAFTA 2011")
    private String awards;

    @Size(max = 255, message = "Known for cannot exceed 255 characters")
    @Column(name = "known_for")
    @Schema(description = "Known for", example = "Inception, Interstellar")
    private String knownFor;

    @Size(max = 50, message = "Years active cannot exceed 50 characters")
    @Column(name = "years_active")
    @Schema(description = "Years active", example = "1998-present")
    private String yearsActive;

    @Size(max = 100, message = "Style cannot exceed 100 characters")
    @Column(name = "style")
    @Schema(description = "Directing style", example = "Non-linear storytelling")
    private String style;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
