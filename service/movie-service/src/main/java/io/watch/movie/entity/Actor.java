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

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "actors")
@Schema(description = "Actor entity representing an actor")
public class Actor extends EntityBase {

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

}
