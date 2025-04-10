package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request object for a season within a series")
public class SeasonRequest {

    @Min(1)
    @Schema(description = "Season number", example = "1")
    private Integer seasonNumber;

    @Size(max = 255)
    @Schema(description = "Title of the season", example = "Season 1")
    private String title;

    @Schema(description = "Description of the season", example = "The first season")
    private String description;

    @PastOrPresent
    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @Size(max = 255)
    @Schema(description = "Poster URL", example = "https://poster.com/season1")
    private String posterUrl;

    @Size(max = 255)
    @Schema(description = "Trailer URL", example = "https://trailer.com/season1")
    private String trailerUrl;

    @Schema(description = "List of episodes")
    private List<EpisodeRequest> episodes;
}