package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Generic item for relations like categories, actors, etc.")
public class RelationItem {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID id;

    @Schema(example = "Sci-Fi")
    private String name;
}
