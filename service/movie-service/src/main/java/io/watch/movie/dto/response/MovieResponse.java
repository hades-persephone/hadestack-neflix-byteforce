package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.*;
import jakarta.persistence.Column;
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
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "releaseDate")
    private LocalDate releaseDate;

    @Column(name = "rating_score")
    private Double ratingScore;

    @Column(name = "imdb_rating")
    private Double imdbRating;

    @Column(name = "rotten_tomatoes_score")
    private Integer rottenTomatoesScore;

    @Column(name = "production_company")
    private String productionCompany;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "box_office")
    private Long boxOffice;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "video_quality")
    private String videoQuality;

    @Column(name = "age_rating")
    private String ageRating;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "runtime_seconds")
    private Integer runtimeSeconds;

    @Column(name = "categories")
    private String categoryNames;

    @Column(name = "actors")
    private String actorNames;

    @Column(name = "directors")
    private String directorNames;

    @Column(name = "languages")
    private String languageNames;

}
