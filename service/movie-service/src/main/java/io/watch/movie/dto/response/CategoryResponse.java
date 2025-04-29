package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for category")
public class CategoryResponse {

    @Schema(description = "Unique identifier of the category", example = "1a2b3c4d...")
    private UUID id;

    @Schema(description = "Name of the category", example = "Sci-Fi")
    private String name;

    @Schema(description = "Description of the category", example = "Science fiction movies and series")
    private String description;

    @Schema(description = "URL of the category icon", example = "https://example.com/icon.png")
    private String iconUrl;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the category was created", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the category was updated", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "Timestamp when the category was deleted", example = "null")
    private LocalDateTime deletedAt;

    @Schema(description = "Parent category ID", example = "uuid...")
    private UUID parentCategoryId;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Created by user ID", example = "1")
    private UUID createdBy;

    @Schema(description = "Updated by user ID", example = "2")
    private UUID updatedBy;
}
