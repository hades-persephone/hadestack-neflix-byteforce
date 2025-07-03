package io.watch.search.util;

import java.util.Map;

public class SearchConfigUtil {
    public static final Map<String, Float> FIELD_BOOSTS = Map.of(
            "title", 3.0f,
            "description", 1.5f,
            "genre", 0.8f,
            "director", 1.0f,
            "actors", 0.6f
    );
    public static final int MAX_SUGGESTION_SIZE = 5;
    public static final int MAX_AGGREGATION_SIZE = 20;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT_FIELD = "_score";
    public static final String DEFAULT_SCROLL_TIMEOUT = "1m";
}
