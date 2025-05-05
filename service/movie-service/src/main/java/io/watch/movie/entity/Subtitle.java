package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subtitles")
@Schema(description = "Subtitle entity representing a subtitle file")
public class Subtitle extends EntityBase {

    @NotBlank(message = "Language cannot be blank")
    @Size(min = 2, max = 50, message = "Language must be between 2 and 50 characters")
    @Column(name = "language", nullable = false)
    @Schema(description = "Language of the subtitle", example = "English")
    private String language;

    @Size(max = 255, message = "File path cannot exceed 255 characters")
    @Column(name = "file_path")
    @Schema(description = "Path to the subtitle file", example = "/var/subtitles/english_subtitle.srt")
    private String filePath;
}
