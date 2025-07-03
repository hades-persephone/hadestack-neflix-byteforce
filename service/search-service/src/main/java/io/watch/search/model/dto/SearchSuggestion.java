package io.watch.search.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchSuggestion {
    private String type;
    private Double score;
    private String text;
    private long freq;
}
