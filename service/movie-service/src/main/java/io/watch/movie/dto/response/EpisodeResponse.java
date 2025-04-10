package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.VideoQuality;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Response object for episode data")
public class EpisodeResponse {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID id;

    @Schema(example = "1")
    private Integer episodeNumber;

    @Schema(example = "Chapter One: The Vanishing")
    private String title;

    @Schema(example = "The kids encounter a mystery")
    private String description;

    @Schema(example = "50")
    private Integer duration;

    @Schema(example = "2016-07-15")
    private LocalDate releaseDate;

    @Schema(example = "8.5")
    private Double ratingScore;

    @Schema(example = "200")
    private Long viewCount;

    @Schema(example = "https://stream.com/s1e1")
    private String streamUrl;

    @Schema(example = "https://thumb.com/s1e1")
    private String thumbnailUrl;

    @Schema(example = "5368709120")
    private Long fileSize;

    @Schema(example = "UHD_4K")
    private VideoQuality videoQuality;

    @Schema(example = "2016-07-15")
    private LocalDate airDate;

    @Schema(example = "3000")
    private Integer runtimeSeconds;

    @Schema(example = "true")
    private Boolean isAvailable;

    @Schema(example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;
}