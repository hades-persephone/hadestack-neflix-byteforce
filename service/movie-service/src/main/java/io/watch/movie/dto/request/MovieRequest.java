package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Movie creation or update request")
public class MovieRequest {

    @Schema(description = "Movie ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Title of the movie", example = "Inception")
    private String title;

    @Schema(description = "Description of the movie", example = "A thief who steals secrets.")
    private String description;

    @Schema(description = "Duration in minutes", example = "148")
    private int duration;

    @Schema(description = "Release date", example = "2010-07-16")
    private LocalDate releaseDate;

    @Schema(description = "Release start date (use for search filter)", example = "2010-07-16")
    private LocalDate releaseStartDate;

    @Schema(description = "Release end date (use for search filter)", example = "2010-07-16")
    private LocalDate releaseEndDate;

    @Schema(description = "Rating score", example = "8.8")
    private Double ratingScore;

    @Schema(description = "IMDb rating", example = "8.8")
    private Double imdbRating;

    @Schema(description = "Rotten Tomatoes score", example = "87")
    private Integer rottenTomatoesScore;

    @Schema(description = "Production company", example = "Warner Bros.")
    private String productionCompany;

    @Schema(description = "Budget in USD", example = "160000000")
    private Long budget;

    @Schema(description = "Box office revenue", example = "829895144")
    private Long boxOffice;

    @Schema(description = "Trailer URL", example = "https://trailer.com/inception")
    private String trailerUrl;

    @Schema(description = "Poster URL", example = "https://poster.com/inception")
    private String posterUrl;

    @Schema(description = "Thumbnail URL", example = "https://thumb.com/inception")
    private String thumbnailUrl;

    @Schema(description = "Video quality", example = "UHD_4K")
    private String videoQuality;

    @Schema(description = "Age rating", example = "PG-13")
    private String ageRating;

    @Schema(description = "Country of origin", example = "USA")
    private String countryOfOrigin;

    @Schema(description = "Availability flag", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Stream URL", example = "https://stream.com/inception")
    private String streamUrl;

    @Schema(description = "File size in bytes", example = "5368709120")
    private Long fileSize;

    @Schema(description = "Runtime in seconds", example = "8880")
    private Integer runtimeSeconds;

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
