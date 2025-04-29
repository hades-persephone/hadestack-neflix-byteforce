package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for language")
public class LanguageResponse {

    @Schema(description = "Unique identifier of the language", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Name of the language", example = "English")
    private String name;

    @Schema(description = "Language code", example = "en")
    private String code;

    @Schema(description = "Whether the language is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "Deletion timestamp", example = "null")
    private LocalDateTime deletedAt;

    @Schema(description = "Region", example = "North America")
    private String region;

    @Schema(description = "Native name", example = "English")
    private String nativeName;

    @Schema(description = "Popularity score", example = "100")
    private Integer popularityScore;

    @Schema(description = "ID of user who created this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID createdBy;

    @Schema(description = "ID of user who updated this record", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID updatedBy;
}
