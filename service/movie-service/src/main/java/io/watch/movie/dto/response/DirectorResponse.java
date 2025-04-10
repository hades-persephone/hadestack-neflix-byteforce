package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for director")
public class DirectorResponse {

    @Schema(description = "Unique identifier of the director", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name of the director", example = "Christopher Nolan")
    private String fullName;

    @Schema(description = "Date of birth", example = "1970-07-30")
    private LocalDate dateOfBirth;

    @Schema(description = "Nationality", example = "UK")
    private String nationality;

    @Schema(description = "Biography", example = "A visionary director known for...")
    private String biography;

    @Schema(description = "Profile picture URL", example = "https://example.com/nolan.jpg")
    private String profilePictureUrl;

    @Schema(description = "Whether the director is active", example = "true")
    private Boolean isActive;

    @Schema(description = "IMDb profile URL", example = "https://imdb.com/name/nm0634240")
    private String imdbProfileUrl;

    @Schema(description = "Awards won", example = "BAFTA 2011")
    private String awards;

    @Schema(description = "Known for", example = "Inception, Interstellar")
    private String knownFor;

    @Schema(description = "Years active", example = "1998-present")
    private String yearsActive;

    @Schema(description = "Directing style", example = "Non-linear storytelling")
    private String style;

    @Schema(description = "Timestamp when the director was created", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the director was updated", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "Timestamp when the director was deleted", example = "null")
    private LocalDateTime deletedAt;

    @Schema(description = "Created by user ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "Updated by user ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
