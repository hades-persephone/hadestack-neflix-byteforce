package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Table(name = "subtitles")
@Schema(description = "Subtitle entity representing a subtitle file")
public class Subtitle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @Schema(description = "Unique identifier of the subtitle", example = "1")
    private UUID id;

    @NotBlank(message = "Language cannot be blank")
    @Size(min = 2, max = 50, message = "Language must be between 2 and 50 characters")
    @Column(name = "language", nullable = false)
    @Schema(description = "Language of the subtitle", example = "English")
    private String language;

    @Size(max = 255, message = "File URL cannot exceed 255 characters")
    @Column(name = "file_url")
    @Schema(description = "URL of the subtitle file", example = "https://example.com/subtitle.srt")
    private String fileUrl;
}
