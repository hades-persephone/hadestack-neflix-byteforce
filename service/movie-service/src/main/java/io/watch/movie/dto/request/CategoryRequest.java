package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating/updating a category")
public class CategoryRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Schema(description = "Name of the category", example = "Sci-Fi")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Description of the category", example = "Science fiction movies and series")
    private String description;

    @Size(max = 255, message = "Icon URL cannot exceed 255 characters")
    @Schema(description = "URL of the category icon", example = "https://example.com/icon.png")
    private String iconUrl;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Parent category ID (nullable)", example = "1a2b3c4d-5e6f-7g8h-9i10-j11k12l13m14")
    private UUID parentCategoryId;

    @Schema(description = "Order for displaying the category", example = "1")
    private Integer displayOrder;

    @Schema(description = "ID of the user who created this category", example = "1")
    private UUID createdBy;

    @Schema(description = "ID of the user who last updated this category", example = "2")
    private UUID updatedBy;

}
