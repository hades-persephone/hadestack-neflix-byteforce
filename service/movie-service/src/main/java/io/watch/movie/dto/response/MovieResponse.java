package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Movie response DTO")
public class MovieResponse {

    @Schema(description = "Movie ID")
    private UUID id;

    private String title;
    private String description;
    private int duration;
    private LocalDate releaseDate;
    private Double ratingScore;
    private Double imdbRating;
    private Integer rottenTomatoesScore;
    private String productionCompany;
    private Long budget;
    private Long boxOffice;
    private String trailerUrl;
    private String posterUrl;
    private String thumbnailUrl;
    private String videoQuality;
    private String ageRating;
    private String countryOfOrigin;
    private Boolean isAvailable;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String streamUrl;
    private Long fileSize;
    private Integer runtimeSeconds;

    private Set<Category> categories;
    private Set<Actor> actors;
    private Set<Director> directors;
    private Set<Language> languages;
    private Set<Subtitle> subtitles;
}
