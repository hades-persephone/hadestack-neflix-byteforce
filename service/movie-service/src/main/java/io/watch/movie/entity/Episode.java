package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.VideoQuality;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "episodes")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Episode entity representing an episode of a season")
public class Episode extends EntityBase {

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    @Schema(description = "Season this episode belongs to")
    private Season season;

    @Min(1)
    @Column(name = "episode_number", nullable = false)
    @Schema(description = "Episode number", example = "1")
    private Integer episodeNumber;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    @Schema(description = "Title of the episode", example = "Chapter One: The Vanishing")
    private String title;

    @Column(name = "description")
    @Schema(description = "Description of the episode", example = "The kids encounter a mystery")
    private String description;

    @Min(1)
    @Column(name = "duration", nullable = false)
    @Schema(description = "Duration in minutes", example = "50")
    private Integer duration;

    @PastOrPresent
    @Column(name = "release_date")
    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @DecimalMin("0.0") @DecimalMax("10.0")
    @Column(name = "rating_score")
    @Schema(description = "Rating score", example = "8.5")
    private Double ratingScore;

    @Min(0)
    @Column(name = "view_count")
    @Schema(description = "View count", example = "200")
    private Long viewCount = 0L;

    @Size(max = 255)
    @Column(name = "stream_url")
    @Schema(description = "Stream URL", example = "https://stream.com/s1e1")
    private String streamUrl;

    @Size(max = 255)
    @Column(name = "thumbnail_url")
    @Schema(description = "Thumbnail URL", example = "https://thumb.com/s1e1")
    private String thumbnailUrl;

    @Min(0)
    @Column(name = "file_size")
    @Schema(description = "File size in bytes", example = "5368709120")
    private Long fileSize;

    @Column(name = "video_quality")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Video quality", example = "UHD_4K")
    private VideoQuality videoQuality;

    @PastOrPresent
    @Column(name = "air_date")
    @Schema(description = "Air date", example = "2016-07-15")
    private LocalDate airDate;

    @Min(0)
    @Column(name = "runtime_seconds")
    @Schema(description = "Runtime in seconds", example = "3000")
    private Integer runtimeSeconds;

}
