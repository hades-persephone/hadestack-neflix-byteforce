package io.watch.rating.dto;

import io.watch.rating.entity.Rating;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class RatingPage {
    private List<Rating> content;
    private boolean hasNext;
    private boolean hasPrevious;
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;
}
