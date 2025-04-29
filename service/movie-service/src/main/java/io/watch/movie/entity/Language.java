package io.watch.movie.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.ActionType;
import io.watch.movie.util.ValidEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "languages")
@Schema(description = "Language entity representing a language")
public class Language extends EntityBase {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "Name of the language", example = "English")
    private String name;

    @NotBlank(message = "Code cannot be blank")
    @Size(min = 2, max = 10, message = "Code must be between 2 and 10 characters")
    @Column(name = "code", nullable = false)
    @Schema(description = "Language code", example = "en")
    private String code;

    @Size(max = 50, message = "Region cannot exceed 50 characters")
    @Column(name = "region")
    @Schema(description = "Region", example = "North America")
    private String region;

    @Size(max = 50, message = "Native name cannot exceed 50 characters")
    @Column(name = "native_name")
    @Schema(description = "Native name", example = "English")
    private String nativeName;

    @Min(value = 0, message = "Popularity score cannot be negative")
    @Column(name = "popularity_score")
    @Schema(description = "Popularity score", example = "100")
    private Integer popularityScore;

}