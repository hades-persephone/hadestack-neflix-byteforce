package io.watch.search.model.dto;

import io.watch.search.util.SearchConfigUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AdvancedSearchRequest {
    private String keyword;
    private List<String> genres;
    private List<String> languages;
    private String rating;
    private Integer fromYear;
    private Integer toYear;
    private Double minScore;
    private Double maxScore;
    private Integer minDuration;
    private Integer maxDuration;
    private Double latitude;
    private Double longitude;
    private Integer radius;
    private int page = 0;
    private int size = SearchConfigUtil.DEFAULT_PAGE_SIZE;
    private String sortBy = SearchConfigUtil.DEFAULT_SORT_FIELD;
    private boolean ascending = false;
    private boolean enableHighlighting = true;
    private boolean includeAggregations = false;
    private boolean includeSuggestions = false;
    private boolean enableWildcardSearch = false;
    private boolean useBoostScoring = false;
    private boolean boostPopular = false;
    private String languageAnalyzer;
    private UserPreferences userPreferences;
    private Long userId;
    private Integer suggestionSize;
    private Long profileId;

    public String cacheKey() {
        return String.format("%s_%s_%d_%d_%s_%b_%s",
                keyword != null ? keyword : "",
                genres != null ? String.join(",", genres) : "",
                page, size, sortBy, ascending,
                userId != null ? userId : "");
    }
}