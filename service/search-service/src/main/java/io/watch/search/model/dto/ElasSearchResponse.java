package io.watch.search.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ElasSearchResponse {
    private List<MovieResult> results;
    private int totalResults;
    private int page;
    private int totalPages;

    @Data
    public static class MovieResult {
        private Long movieId;
        private String title;
        private String description;
        private String genre;
        private Double score;
        private String highlightTitle;
        private String highlightDescription;
    }
}
