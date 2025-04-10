package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Response object for season data")
public class SeasonResponse {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;

    @Schema(example = "1")
    private Integer seasonNumber;

    @Schema(example = "Season 1")
    private String title;

    @Schema(example = "The first season")
    private String description;

    @Schema(example = "2016-07-15")
    private LocalDate releaseDate;

    @Schema(example = "https://poster.com/season1")
    private String posterUrl;

    @Schema(example = "https://trailer.com/season1")
    private String trailerUrl;

    @Schema(example = "500")
    private Long viewCount;

    @Schema(example = "true")
    private Boolean isAvailable;

    @Schema(example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "List of episodes")
    private List<EpisodeResponse> episodes;
}