package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Movie creation or update request")
public class MovieRequest {

    @Schema(description = "Movie ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the movie", example = "Inception")
    private String title;

    @Schema(description = "Description of the movie", example = "A thief who steals secrets.")
    private String description;

    @Schema(description = "Duration in minutes", example = "148")
    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int duration;

    @PastOrPresent(message = "Release date cannot be in the future")
    @Schema(description = "Release date", example = "2010-07-16")
    private LocalDate releaseDate;

    @Schema(description = "Release start date (use for search filter)", example = "2010-07-16")
    private LocalDate releaseStartDate;

    @Schema(description = "Release end date (use for search filter)", example = "2010-07-16")
    private LocalDate releaseEndDate;

    @Schema(description = "Rating score", example = "8.8")
    @Min(value = 0, message = "Rating score must be at least 0")
    @Max(value = 10, message = "Rating score must not exceed 10")
    private Double ratingScore;

    @Min(value = 0, message = "IMDb rating must be at least 0")
    @Max(value = 10, message = "IMDb rating must not exceed 10")
    @Schema(description = "IMDb rating", example = "8.8")
    private Double imdbRating;

    @Min(value = 0, message = "Rotten Tomatoes score must be at least 0")
    @Max(value = 100, message = "Rotten Tomatoes score must not exceed 100")
    @Schema(description = "Rotten Tomatoes score", example = "87")
    private Integer rottenTomatoesScore;

    @Schema(description = "Production company", example = "Warner Bros.")
    @Size(max = 255, message = "Production company must not exceed 255 characters")
    private String productionCompany;

    @Min(value = 0, message = "Budget cannot be negative")
    @Schema(description = "Budget in USD", example = "160000000")
    private Long budget;

    @Min(value = 0, message = "Box office revenue cannot be negative")
    @Schema(description = "Box office revenue", example = "829895144")
    private Long boxOffice;

    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$", message = "Invalid trailer URL format")
    @Schema(description = "Trailer URL", example = "https://trailer.com/inception")
    private String trailerUrl;

    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$", message = "Invalid trailer URL format")
    @Schema(description = "Poster URL", example = "https://poster.com/inception")
    private String posterUrl;

    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$", message = "Invalid trailer URL format")
    @Schema(description = "Thumbnail URL", example = "https://thumb.com/inception")
    private String thumbnailUrl;

    @Schema(description = "Video quality", example = "UHD_4K")
    private String videoQuality;

    @Size(max = 10, message = "Age rating must not exceed 10 characters")
    @Schema(description = "Age rating", example = "PG-13")
    private String ageRating;

    @Size(max = 100, message = "Country of origin must not exceed 100 characters")
    @Schema(description = "Country of origin", example = "USA")
    private String countryOfOrigin;

    @NotNull(message = "Availability flag cannot be null")
    @Schema(description = "Availability flag", example = "true")
    private Boolean isAvailable;

    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$", message = "Invalid trailer URL format")
    @Schema(description = "Stream URL", example = "https://stream.com/inception")
    private String streamUrl;

    @Min(value = 0, message = "File size cannot be negative")
    @Schema(description = "File size in bytes", example = "5368709120")
    private Long fileSize;

    @Min(value = 0, message = "Runtime cannot be negative")
    @Schema(description = "Runtime in seconds", example = "8880")
    private Integer runtimeSeconds;

    @NotEmpty(message = "Category IDs cannot be empty")
    @Schema(description = "Category IDs")
    private Set<UUID> categoryIds;

    @Schema(description = "Actor IDs")
    private Set<UUID> actorIds;

    @Schema(description = "Director IDs")
    private Set<UUID> directorIds;

    @Schema(description = "Language IDs")
    private Set<UUID> languageIds;

    @Schema(description = "Subtitle IDs")
    private Set<UUID> subtitleIds;
}
