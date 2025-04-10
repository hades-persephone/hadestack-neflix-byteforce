package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating or updating a director")
public class DirectorRequest {

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name of the director", example = "Christopher Nolan")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "1970-07-30")
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    @Schema(description = "Nationality", example = "UK")
    private String nationality;

    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    @Schema(description = "Biography", example = "A visionary director known for...")
    private String biography;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    @Schema(description = "Profile picture URL", example = "https://example.com/nolan.jpg")
    private String profilePictureUrl;

    @Schema(description = "Whether the director is active", example = "true")
    private Boolean isActive;

    @Size(max = 255, message = "IMDb profile URL cannot exceed 255 characters")
    @Schema(description = "IMDb profile URL", example = "https://imdb.com/name/nm0634240")
    private String imdbProfileUrl;

    @Size(max = 2000, message = "Awards cannot exceed 2000 characters")
    @Schema(description = "Awards won", example = "BAFTA 2011")
    private String awards;

    @Size(max = 255, message = "Known for cannot exceed 255 characters")
    @Schema(description = "Known for", example = "Inception, Interstellar")
    private String knownFor;

    @Size(max = 50, message = "Years active cannot exceed 50 characters")
    @Schema(description = "Years active", example = "1998-present")
    private String yearsActive;

    @Size(max = 100, message = "Style cannot exceed 100 characters")
    @Schema(description = "Directing style", example = "Non-linear storytelling")
    private String style;
}
