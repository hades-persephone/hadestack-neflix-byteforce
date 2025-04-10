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
@Table(name = "seasons")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Season entity representing a season of a series")
public class Season extends EntityBase {

    @ManyToOne
    @JoinColumn(name = "series_id", nullable = false)
    @Schema(description = "Series this season belongs to")
    private Series series;

    @Min(1)
    @Column(name = "season_number", nullable = false)
    @Schema(description = "Season number", example = "1")
    private Integer seasonNumber;

    @Size(max = 255)
    @Column(name = "title")
    @Schema(description = "Title of the season", example = "Season 1")
    private String title;

    @Column(name = "description")
    @Schema(description = "Description of the season", example = "The first season")
    private String description;

    @PastOrPresent
    @Column(name = "release_date")
    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @Size(max = 255)
    @Column(name = "poster_url")
    @Schema(description = "Poster URL", example = "https://poster.com/season1")
    private String posterUrl;

    @Size(max = 255)
    @Column(name = "trailer_url")
    @Schema(description = "Trailer URL", example = "https://trailer.com/season1")
    private String trailerUrl;

    @Min(0)
    @Column(name = "view_count")
    @Schema(description = "View count", example = "500")
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private Set<Episode> episodes;
}