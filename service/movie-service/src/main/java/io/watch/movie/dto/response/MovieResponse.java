package io.watch.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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
    private UUID id;
    private String title;
    private String description;
    private Integer duration;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "release_date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate releaseDate;

    private Double ratingScore;
    private Double imdbRating;
    private Integer rottenTomatoesScore;
    private String productionCompany;
    private Long budget;
    private Long boxOffice;
    private String trailerUrl;
    private String posterUrl;
    private String thumbnailUrl;
    private String videoQuality;
    private String ageRating;
    private String countryOfOrigin;
    private Long viewCount;
    private String streamUrl;
    private Long fileSize;
    private Integer runtimeSeconds;
    private String categoryNames;
    private String actorNames;
    private String directorNames;
    private String languageNames;
    private LocalDateTime createdAt;

}
