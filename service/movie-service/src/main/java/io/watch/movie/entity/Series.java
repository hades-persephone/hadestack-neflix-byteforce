package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "series")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Series entity representing a TV series")
public class Series extends EntityBase {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    @Schema(description = "Title of the series", example = "Stranger Things")
    private String title;

    @Column(name = "description")
    @Schema(description = "Description of the series", example = "A sci-fi series")
    private String description;

    @PastOrPresent
    @Column(name = "release_date")
    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @DecimalMin("0.0") @DecimalMax("10.0")
    @Column(name = "rating_score")
    @Schema(description = "Rating score", example = "8.7")
    private Double ratingScore;

    @DecimalMin("0.0") @DecimalMax("10.0")
    @Column(name = "imdb_rating")
    @Schema(description = "IMDb rating", example = "8.7")
    private Double imdbRating;

    @Min(0) @Max(100)
    @Column(name = "rotten_tomatoes_score")
    @Schema(description = "Rotten Tomatoes score", example = "93")
    private Integer rottenTomatoesScore;

    @Size(max = 100)
    @Column(name = "production_company")
    @Schema(description = "Production company", example = "Netflix")
    private String productionCompany;

    @Size(max = 255)
    @Column(name = "trailer_url")
    @Schema(description = "Trailer URL", example = "https://trailer.com/stranger-things")
    private String trailerUrl;

    @Size(max = 255)
    @Column(name = "poster_url")
    @Schema(description = "Poster URL", example = "https://poster.com/stranger-things")
    private String posterUrl;

    @Size(max = 255)
    @Column(name = "thumbnail_url")
    @Schema(description = "Thumbnail URL", example = "https://thumb.com/stranger-things")
    private String thumbnailUrl;

    @Size(max = 10)
    @Column(name = "age_rating")
    @Schema(description = "Age rating", example = "TV-14")
    private String ageRating;

    @Size(max = 50)
    @Column(name = "country_of_origin")
    @Schema(description = "Country of origin", example = "USA")
    private String countryOfOrigin;

    @Min(0)
    @Column(name = "view_count")
    @Schema(description = "View count", example = "1000")
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL)
    private Set<Season> seasons;

    @ManyToMany
    @JoinTable(name = "series_categories",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @ManyToMany
    @JoinTable(name = "series_actors",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id"))
    private Set<Actor> actors;

    @ManyToMany
    @JoinTable(name = "series_directors",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "director_id"))
    private Set<Director> directors;

    @ManyToMany
    @JoinTable(name = "series_languages",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id"))
    private Set<Language> languages;

    @ManyToMany
    @JoinTable(name = "series_subtitles",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "subtitle_id"))
    private Set<Subtitle> subtitles;

    @ManyToMany
    @JoinTable(name = "playlists_series",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id"))
    private Set<Playlist> playlists;
}
