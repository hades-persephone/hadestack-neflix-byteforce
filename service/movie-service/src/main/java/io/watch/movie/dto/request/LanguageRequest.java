package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating/updating a language")
public class LanguageRequest {

    @Schema(description = "Name of the language", example = "English")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Schema(description = "Language code", example = "en")
    @NotBlank(message = "Code cannot be blank")
    @Size(min = 2, max = 10, message = "Code must be between 2 and 10 characters")
    private String code;

    @Schema(description = "Whether the language is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Region", example = "North America")
    @Size(max = 50, message = "Region cannot exceed 50 characters")
    private String region;

    @Schema(description = "Native name", example = "English")
    @Size(max = 50, message = "Native name cannot exceed 50 characters")
    private String nativeName;

    @Schema(description = "Popularity score", example = "100")
    @Min(value = 0, message = "Popularity score cannot be negative")
    private Integer popularityScore;
}
