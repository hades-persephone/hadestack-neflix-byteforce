package io.watch.search.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScrollSearchResult {
    private String scrollId;
    private List<ElasSearchResponse.MovieResult> results;
    private long totalHits;

    public ScrollSearchResult(String scrollId, List<ElasSearchResponse.MovieResult> results, long totalHits) {
        this.scrollId = scrollId;
        this.results = results;
        this.totalHits = totalHits;
    }

    public boolean shouldClearScroll() {
        return results.isEmpty() || scrollId == null;
    }
}