package io.watch.movie.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.*;
import io.watch.movie.util.ValidEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "movies")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Movie entity representing a single movie")
public class Movie extends EntityBase {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "title", nullable = false)
    @Schema(description  = "Title of the movie", example = "Inception")
    private String title;

    @Column(unique = true, nullable = false, length = 50)
    @Size(max = 50, message = "code cannot exceed 50 characters")
    @Schema(description  = "Code of the movie", example = "HSNB-SPIDERMAN-7H4R")
    private String code;

    @Column(name = "description")
    @Schema(description  = "Description of the movie", example = "A thief who steals secrets.")
    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(name = "duration", nullable = false)
    @Schema(description  = "Duration in minutes", example = "148")
    private int duration;

    //    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @PastOrPresent(message = "Release date must be in the past or present")
    @Column(name = "release_date")
    @Schema(description  = "Release date", example = "2010-07-16")
    @Temporal(TemporalType.DATE)
    private LocalDate releaseDate;

    @DecimalMin(value  = "0.0") @DecimalMax(value  = "10.0")
    @Column(name = "rating_score")
    @Schema(description  = "Rating score", example = "8.8")
    private Double ratingScore;

    @DecimalMin(value  = "0.0") @DecimalMax(value  = "10.0")
    @Column(name = "imdb_rating")
    @Schema(description  = "IMDb rating", example = "8.8")
    private Double imdbRating;

    @Min(value  = 0)
    @Max(value  = 100)
    @Column(name = "rotten_tomatoes_score")
    @Schema(description  = "Rotten Tomatoes score", example = "87")
    private Integer rottenTomatoesScore;

    @Size(max = 100, message = "Production company cannot exceed 100 characters")
    @Column(name = "production_company")
    @Schema(description  = "Production company", example = "Warner Bros.")
    private String productionCompany;

    @Min(value  = 0, message = "Budget cannot be negative")
    @Column(name = "budget")
    @Schema(description  = "Budget in USD", example = "160000000")
    private Long budget;

    @Min(value  = 0, message = "Box office cannot be negative")
    @Column(name = "box_office")
    @Schema(description  = "Box office revenue", example = "829895144")
    private Long boxOffice;

    @Size(max = 255, message = "Trailer URL cannot exceed 255 characters")
    @Column(name = "trailer_url")
    @Schema(description  = "Trailer URL", example = "https://trailer.com/inception")
    private String trailerUrl;

    @Size(max = 255, message = "Poster URL cannot exceed 255 characters")
    @Column(name = "poster_url")
    @Schema(description  = "Poster URL", example = "https://poster.com/inception")
    private String posterUrl;

    @Size(max = 255, message = "Thumbnail URL cannot exceed 255 characters")
    @Column(name = "thumbnail_url")
    @Schema(description  = "Thumbnail URL", example = "https://thumb.com/inception")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_quality")
    @Schema(description  = "Video quality", example = "UHD_4K")
    private VideoQuality videoQuality;

    @Column(name = "age_rating")
    @Schema(description  = "Age rating", example = "PG-13")
    @Enumerated(EnumType.STRING)
    private AgeRating ageRating;

    @Size(max = 50, message = "Country of origin cannot exceed 50 characters")
    @Column(name = "country_of_origin")
    @Schema(description  = "Country of origin", example = "USA")
    private String countryOfOrigin;

    @Min(value  = 0, message = "View count cannot be negative")
    @Column(name = "view_count")
    @Schema(description  = "View count", example = "1000")
    private Long viewCount = 0L;

    @Size(max = 255, message = "Stream URL cannot exceed 255 characters")
    @Column(name = "stream_url")
    @Schema(description  = "Stream URL", example = "https://stream.com/inception")
    private String streamUrl;

    @Min(value  = 0, message = "File size cannot be negative")
    @Column(name = "file_size")
    @Schema(description  = "File size in bytes", example = "5368709120")
    private Long fileSize;

    @Min(value  = 0, message = "Runtime cannot be negative")
    @Column(name = "runtime_seconds")
    @Schema(description  = "Runtime in seconds", example = "8880")
    private Integer runtimeSeconds;

    @ManyToMany
    @JoinTable(name = "movies_categories",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @ManyToMany
    @JoinTable(name = "movies_actors",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id"))
    private Set<Actor> actors;

    @ManyToMany
    @JoinTable(name = "movies_directors",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "director_id"))
    private Set<Director> directors;

    @ManyToMany
    @JoinTable(name = "movies_languages",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id"))
    private Set<Language> languages;

    @ManyToMany
    @JoinTable(name = "movies_subtitles",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "subtitle_id"))
    private Set<Subtitle> subtitles;
}
