package io.watch.search.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class SearchAnalytics {
    private Map<String, Long> genreDistribution;
    private String query;
    private LocalDateTime searchTime;
    private boolean timedOut;
    private long tookMillis;
    private Double maxScore;
    private long totalHits;
}
