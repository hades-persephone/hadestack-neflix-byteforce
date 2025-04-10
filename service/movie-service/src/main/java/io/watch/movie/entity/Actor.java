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
@Table(name = "actors")
@Schema(description = "Actor entity representing an actor")
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the actor", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    @Schema(description = "Full name of the actor", example = "Leonardo DiCaprio")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    @Schema(description = "Date of birth", example = "1974-11-11")
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    @Column(name = "nationality")
    @Schema(description = "Nationality", example = "USA")
    private String nationality;

    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    @Column(name = "biography")
    @Schema(description = "Biography", example = "An acclaimed actor known for...")
    private String biography;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    @Column(name = "profile_picture_url")
    @Schema(description = "Profile picture URL", example = "https://example.com/leo.jpg")
    private String profilePictureUrl;

    @Min(value = 0, message = "Height must be non-negative")
    @Column(name = "height_cm")
    @Schema(description = "Height in centimeters", example = "183")
    private Integer heightCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @Schema(description = "Gender", example = "MALE")
    private Gender gender;

    @Column(name = "is_active")
    @Schema(description = "Whether the actor is active", example = "true")
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
    @Schema(description = "IMDb profile URL", example = "https://imdb.com/name/nm0000138")
    private String imdbProfileUrl;

    @Size(max = 2000, message = "Awards cannot exceed 2000 characters")
    @Column(name = "awards")
    @Schema(description = "Awards won", example = "Oscar 2016")
    private String awards;

    @Size(max = 255, message = "Known for cannot exceed 255 characters")
    @Column(name = "known_for")
    @Schema(description = "Known for", example = "Inception, Titanic")
    private String knownFor;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
