package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.Season;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "Request object for creating/updating a series with seasons and episodes")
public class SeriesRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255)
    @Schema(description = "Title of the series", example = "Stranger Things")
    private String title;

    @Schema(description = "Description of the series", example = "A sci-fi series")
    private String description;

    @PastOrPresent
    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @DecimalMin("0.0") @DecimalMax("10.0")
    @Schema(description = "Rating score", example = "8.7")
    private Double ratingScore;

    @DecimalMin("0.0") @DecimalMax("10.0")
    @Schema(description = "IMDb rating", example = "8.7")
    private Double imdbRating;

    @Min(0) @Max(100)
    @Schema(description = "Rotten Tomatoes score", example = "93")
    private Integer rottenTomatoesScore;

    @Size(max = 100)
    @Schema(description = "Production company", example = "Netflix")
    private String productionCompany;

    @Size(max = 255)
    @Schema(description = "Trailer URL", example = "https://trailer.com/stranger-things")
    private String trailerUrl;

    @Size(max = 255)
    @Schema(description = "Poster URL", example = "https://poster.com/stranger-things")
    private String posterUrl;

    @Size(max = 255)
    @Schema(description = "Thumbnail URL", example = "https://thumb.com/stranger-things")
    private String thumbnailUrl;

    @Size(max = 10)
    @Schema(description = "Age rating", example = "TV-14")
    private String ageRating;

    @Size(max = 50)
    @Schema(description = "Country of origin", example = "USA")
    private String countryOfOrigin;

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

    @Schema(description = "Playlist IDs")
    private Set<UUID> playlistIds;

    @Schema(description = "List of seasons")
    private Set<SeasonRequest> seasons;
}