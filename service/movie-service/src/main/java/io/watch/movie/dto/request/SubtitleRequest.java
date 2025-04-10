package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for creating or updating subtitle data")
public class SubtitleRequest {

    @NotBlank(message = "Language cannot be blank")
    @Size(min = 2, max = 50, message = "Language must be between 2 and 50 characters")
    @Schema(description = "Language of the subtitle", example = "English")
    private String language;

    @Size(max = 255, message = "File URL cannot exceed 255 characters")
    @Schema(description = "URL of the subtitle file", example = "https://example.com/subtitle.srt")
    private String fileUrl;
}
