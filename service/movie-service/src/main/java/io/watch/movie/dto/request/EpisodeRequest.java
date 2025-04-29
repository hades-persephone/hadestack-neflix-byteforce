package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.Season;
import io.watch.movie.entity.substraction.VideoQuality;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request object for an episode within a season")
public class EpisodeRequest {

    @Min(1)
    @Schema(description = "Episode number", example = "1")
    private Integer episodeNumber;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255)
    @Schema(description = "Title of the episode", example = "Chapter One: The Vanishing")
    private String title;

    @Schema(description = "Description of the episode", example = "The kids encounter a mystery")
    private String description;

    @Min(1)
    @Schema(description = "Duration in minutes", example = "50")
    private Integer duration;

    @PastOrPresent
    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @DecimalMin("0.0") @DecimalMax("10.0")
    @Schema(description = "Rating score", example = "8.5")
    private Double ratingScore;

    @Size(max = 255)
    @Schema(description = "Stream URL", example = "https://stream.com/s1e1")
    private String streamUrl;

    @Size(max = 255)
    @Schema(description = "Thumbnail URL", example = "https://thumb.com/s1e1")
    private String thumbnailUrl;

    @Min(0)
    @Schema(description = "File size in bytes", example = "5368709120")
    private Long fileSize;

    @Schema(description = "Video quality", example = "UHD_4K")
    private VideoQuality videoQuality;

    @PastOrPresent
    @Schema(description = "Air date", example = "2016-07-15")
    private LocalDate airDate;

    @Min(0)
    @Schema(description = "Runtime in seconds", example = "3000")
    private Integer runtimeSeconds;

    @Schema(description = "Season this episode belongs to")
    private Season season;
}