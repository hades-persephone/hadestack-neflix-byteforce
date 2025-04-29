package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "categories")
@Schema(description = "Category entity representing a movie/series category")
public class Category extends EntityBase {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "Name of the category", example = "Sci-Fi")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description")
    @Schema(description = "Description of the category", example = "Science fiction movies and series")
    private String description;

    @Size(max = 255, message = "Icon URL cannot exceed 255 characters")
    @Column(name = "icon_url")
    @Schema(description = "URL of the category icon", example = "https://example.com/icon.png")
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @Schema(description = "Parent category this category belongs to (if any)")
    private Category parentCategory;

    @Column(name = "display_order")
    @Schema(description = "Order for displaying the category", example = "1")
    private Integer displayOrder;

}