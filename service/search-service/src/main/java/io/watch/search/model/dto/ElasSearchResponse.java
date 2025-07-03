package io.watch.search.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ElasSearchResponse {
    private List<MovieResult> results;
    private int totalResults;
    private int page;
    private int totalPages;
    private Map<String, Object> aggregations;
    private List<SearchSuggestion> suggestions;
    private SearchAnalytics analytics;


    @Data
    public static class MovieResult {
        private Long movieId;
        private String title;
        private String description;
        private String genre;
        private Double score;
        private String highlightTitle;
        private String highlightDescription;
        private Long version;
        private Double relevanceScore;
        private String index;
        private Map<String, List<String>> highlights;
        private Map<String, Object> innerHits;
    }
}
