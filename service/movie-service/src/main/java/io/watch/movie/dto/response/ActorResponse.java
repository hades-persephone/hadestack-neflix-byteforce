package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.Gender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO for returning actor information")
public class ActorResponse {

    @Schema(description = "Unique identifier of the actor", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name of the actor", example = "Leonardo DiCaprio")
    private String fullName;

    @Schema(description = "Date of birth", example = "1974-11-11")
    private LocalDate dateOfBirth;

    @Schema(description = "Nationality of the actor", example = "USA")
    private String nationality;

    @Schema(description = "Brief biography of the actor", example = "An acclaimed actor known for...")
    private String biography;

    @Schema(description = "Profile picture URL", example = "https://example.com/leo.jpg")
    private String profilePictureUrl;

    @Schema(description = "Height in centimeters", example = "183")
    private Integer heightCm;

    @Schema(description = "Gender of the actor", example = "MALE")
    private Gender gender;

    @Schema(description = "Whether the actor is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "IMDb profile URL", example = "https://imdb.com/name/nm0000138")
    private String imdbProfileUrl;

    @Schema(description = "Awards the actor has won", example = "Oscar 2016")
    private String awards;

    @Schema(description = "Notable works of the actor", example = "Inception, Titanic")
    private String knownFor;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "Deletion timestamp", example = "null")
    private LocalDateTime deletedAt;

    @Schema(description = "User ID who created the actor record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "User ID who last updated the actor record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
