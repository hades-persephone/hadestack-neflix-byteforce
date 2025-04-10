package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "DTO for returning subtitle data")
public class SubtitleResponse {

    @Schema(description = "Unique identifier of the subtitle", example = "1")
    private UUID id;

    @Schema(description = "Language of the subtitle", example = "English")
    private String language;

    @Schema(description = "URL of the subtitle file", example = "https://example.com/subtitle.srt")
    private String fileUrl;
}
