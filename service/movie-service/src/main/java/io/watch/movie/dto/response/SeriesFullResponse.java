package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "Full response object for a series with all related data")
public class SeriesFullResponse {
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(example = "Stranger Things")
    private String title;

    @Schema(example = "A sci-fi series")
    private String description;

    @Schema(example = "2016-07-15")
    private LocalDate releaseDate;

    @Schema(example = "8.7")
    private Double ratingScore;

    @Schema(example = "8.7")
    private Double imdbRating;

    @Schema(example = "93")
    private Integer rottenTomatoesScore;

    @Schema(example = "Netflix")
    private String productionCompany;

    @Schema(example = "https://trailer.com/stranger-things")
    private String trailerUrl;

    @Schema(example = "https://poster.com/stranger-things")
    private String posterUrl;

    @Schema(example = "https://thumb.com/stranger-things")
    private String thumbnailUrl;

    @Schema(example = "TV-14")
    private String ageRating;

    @Schema(example = "USA")
    private String countryOfOrigin;

    @Schema(example = "1000")
    private Long viewCount;

    @Schema(example = "SER-0001")
    private String code;

    @Schema(example = "true")
    private Boolean isAvailable;

    @Schema(example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(example = "2025-04-10T10:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "List of categories")
    private Set<RelationItem> categories;

    @Schema(description = "List of actors")
    private Set<RelationItem> actors;

    @Schema(description = "List of directors")
    private Set<RelationItem> directors;

    @Schema(description = "List of languages")
    private Set<RelationItem> languages;

    @Schema(description = "List of subtitles")
    private Set<RelationItem> subtitles;

    @Schema(description = "List of playlists")
    private Set<RelationItem> playlists;

    @Schema(description = "List of seasons")
    private List<SeasonResponse> seasons;
}
