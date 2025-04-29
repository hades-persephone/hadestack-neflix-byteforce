package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.Gender;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating an actor")
public class ActorRequest {

    @Schema(description = "Actor ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name of the actor", example = "Leonardo DiCaprio")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Date of birth", example = "1974-11-11")
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    @Schema(description = "Nationality of the actor", example = "USA")
    private String nationality;

    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    @Schema(description = "Brief biography of the actor", example = "An acclaimed actor known for...")
    private String biography;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    @Schema(description = "Profile picture URL", example = "https://example.com/leo.jpg")
    private String profilePictureUrl;

    @Min(value = 0, message = "Height must be non-negative")
    @Schema(description = "Height in centimeters", example = "183")
    private Integer heightCm;

    @Schema(description = "Gender of the actor", example = "MALE")
    private Gender gender;

    @Schema(description = "Whether the actor is currently active", example = "true")
    private Boolean isActive = true;

    @Size(max = 255, message = "IMDb profile URL cannot exceed 255 characters")
    @Schema(description = "IMDb profile URL", example = "https://imdb.com/name/nm0000138")
    private String imdbProfileUrl;

    @Size(max = 2000, message = "Awards cannot exceed 2000 characters")
    @Schema(description = "Awards the actor has won", example = "Oscar 2016")
    private String awards;

    @Size(max = 255, message = "Known for cannot exceed 255 characters")
    @Schema(description = "Notable works of the actor", example = "Inception, Titanic")
    private String knownFor;

    @Schema(description = "User ID who created the actor record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "User ID who last updated the actor record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
