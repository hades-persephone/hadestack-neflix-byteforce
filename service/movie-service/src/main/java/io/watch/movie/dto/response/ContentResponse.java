package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Response object for both series and movies")
public class ContentResponse {
    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Title", example = "Stranger Things")
    private String title;

    @Schema(description = "Description", example = "A sci-fi series")
    private String description;

    @Schema(description = "Release date", example = "2016-07-15")
    private LocalDate releaseDate;

    @Schema(description = "Rating score", example = "8.7")
    private Double ratingScore;

    @Schema(description = "View count", example = "1000")
    private Long viewCount;

    @Schema(description = "Poster URL", example = "https://poster.com/stranger-things")
    private String posterUrl;

    @Schema(description = "Availability flag", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Unique code", example = "SER-0001")
    private String code;

    @Schema(description = "Type of content", example = "SERIES")
    private String type;
}
