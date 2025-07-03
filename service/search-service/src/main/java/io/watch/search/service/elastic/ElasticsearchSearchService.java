package io.watch.search.service.elastic;


import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SuggestMode;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.watch.search.exception.ElasticsearchSearchException;
import io.watch.search.model.dto.*;
import io.watch.search.model.entity.Movie;
import io.watch.search.util.SearchConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.PhraseSuggester;
import co.elastic.clients.elasticsearch.core.search.TermSuggester;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchAsyncClient asyncClient;
    private final MeterRegistry meterRegistry;

    /**
     * Performs an advanced search on the movies index (synchronous).
     *
     * @param searchRequest the request containing search parameters
     * @return an ElasSearchResponse containing search results and metadata
     * @throws ElasticsearchSearchException if the search fails
     */
    @Cacheable(value = "movieSearch", key = "#searchRequest.cacheKey()", unless = "#result.totalResults == 0")
    public ElasSearchResponse advancedSearchMovies(AdvancedSearchRequest searchRequest) throws IOException {
        Timer timer = Timer.builder("elasticsearch.search")
                .tag("type", "advanced")
                .register(meterRegistry);
        return timer.record(() -> {
            try {
                validateRequest(searchRequest);
                trackSearchTrend(searchRequest);
                BoolQuery.Builder mainQuery = buildAdvancedQuery(searchRequest);

                SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                        .index("movies")
                        .query(q -> q.bool(mainQuery.build()))
                        .from(searchRequest.getPage() * searchRequest.getSize())
                        .size(searchRequest.getSize())
                        .trackTotalHits(TrackHits.of(t -> t.enabled(true)));

                addSorting(searchBuilder, searchRequest);
                addHighlighting(searchBuilder, searchRequest);
                addAggregations(searchBuilder, searchRequest);
                addSuggestions(searchBuilder, searchRequest);

                SearchResponse<Movie> response = elasticsearchClient.search(
                        searchBuilder.build(), Movie.class
                );

                return buildSearchResponse(response, searchRequest);
            } catch (IOException e) {
                meterRegistry.counter("elasticsearch.search.errors", "type", "advanced").increment();
                log.error("Elasticsearch IO error: {}", e.getMessage(), e);
                throw new ElasticsearchSearchException("Failed to execute search query: " + e.getMessage(), e);
            } catch (Exception e) {
                meterRegistry.counter("elasticsearch.search.errors", "type", "advanced").increment();
                log.error("Unexpected error during search: {}", e.getMessage(), e);
                throw new ElasticsearchSearchException("Unexpected error during search: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Performs an advanced search on the movies index (asynchronous).
     *
     * @param searchRequest the request containing search parameters
     * @return a CompletableFuture containing the search response
     */
    public CompletableFuture<ElasSearchResponse> advancedSearchMoviesAsync(AdvancedSearchRequest searchRequest) {
        Timer timer = Timer.builder("elasticsearch.search.async")
                .tag("type", "advanced")
                .register(meterRegistry);
        return timer.record(() -> {
            try {
                validateRequest(searchRequest);
                trackSearchTrend(searchRequest);
                BoolQuery.Builder mainQuery = buildAdvancedQuery(searchRequest);

                SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                        .index("movies")
                        .query(q -> q.bool(mainQuery.build()))
                        .from(searchRequest.getPage() * searchRequest.getSize())
                        .size(searchRequest.getSize())
                        .trackTotalHits(TrackHits.of(t -> t.enabled(true)));

                addSorting(searchBuilder, searchRequest);
                addHighlighting(searchBuilder, searchRequest);
                addAggregations(searchBuilder, searchRequest);
                addSuggestions(searchBuilder, searchRequest);

                return asyncClient.search(searchBuilder.build(), Movie.class)
                        .thenApply(response -> buildSearchResponse(response, searchRequest))
                        .exceptionally(throwable -> {
                            meterRegistry.counter("elasticsearch.search.errors", "type", "advanced.async").increment();
                            log.error("Async search failed: {}", throwable.getMessage(), throwable);
                            throw new ElasticsearchSearchException("Async search failed: " + throwable.getMessage(), throwable);
                        });
            } catch (Exception e) {
                meterRegistry.counter("elasticsearch.search.errors", "type", "advanced.async").increment();
                log.error("Unexpected async error: {}", e.getMessage(), e);
                throw new ElasticsearchSearchException("Unexpected async error: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Validates the search request parameters.
     *
     * @param request the search request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    private void validateRequest(AdvancedSearchRequest request) {
        if (request.getPage() < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (request.getSize() <= 0 || request.getSize() > 1000) {
            throw new IllegalArgumentException("Size must be between 1 and 1000");
        }
        if (request.getKeyword() != null && request.getKeyword().length() > 1000) {
            throw new IllegalArgumentException("Keyword is too long");
        }
    }

    /**
     * Tracks search trends for analytics.
     *
     * @param request the search request
     */
    private void trackSearchTrend(AdvancedSearchRequest request) {
        try {
            elasticsearchClient.index(i -> i
                    .index("search_trends")
                    .document(new SearchTrend(
                            request.getKeyword(),
                            request.getGenres(),
                            LocalDateTime.now(),
                            request.getUserId()
                    ))
            );
        } catch (IOException e) {
            log.warn("Failed to track search trend: {}", e.getMessage());
        }
    }

    /**
     * Builds an advanced query with text search, filters, and personalization.
     *
     * @param request the search request
     * @return a BoolQuery builder
     */
    private BoolQuery.Builder buildAdvancedQuery(AdvancedSearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            addTextSearch(boolQuery, request.getKeyword(), request);
        }

        addFilters(boolQuery, request);
        addRangeQueries(boolQuery, request);
        addGeospatialSearch(boolQuery, request);
        personalizeQuery(boolQuery, request);

        if (request.isUseBoostScoring()) {
            return wrapWithFunctionScore(boolQuery, request);
        }

        return boolQuery;
    }

    /**
     * Adds advanced text search with multi-match and language support.
     */
    private void addTextSearch(BoolQuery.Builder boolQuery, String keyword, AdvancedSearchRequest request) {
        BoolQuery.Builder textQuery = new BoolQuery.Builder();
        String analyzer = request.getLanguageAnalyzer() != null ? request.getLanguageAnalyzer() : "standard";

        textQuery.should(Query.of(q -> q
                .multiMatch(m -> m
                        .fields(SearchConfigUtil.FIELD_BOOSTS.entrySet().stream()
                                .map(e -> e.getKey() + "^" + e.getValue())
                                .collect(Collectors.toList()))
                        .query(keyword)
                        .type(TextQueryType.Phrase)
                        .analyzer(analyzer)
                        .boost(2.0f)
                )
        ));

        textQuery.should(Query.of(q -> q
                .multiMatch(m -> m
                        .fields(SearchConfigUtil.FIELD_BOOSTS.entrySet().stream()
                                .map(e -> e.getKey() + "^" + e.getValue())
                                .collect(Collectors.toList()))
                        .query(keyword)
                        .type(TextQueryType.BestFields)
                        .fuzziness("AUTO")
                        .prefixLength(2)
                        .boost(1.5f)
                )
        ));

        textQuery.should(Query.of(q -> q
                .multiMatch(m -> m
                        .fields(List.of("title", "description"))
                        .query(keyword)
                        .type(TextQueryType.CrossFields)
                        .operator(Operator.And)
                        .analyzer(analyzer)
                        .boost(1.0f)
                )
        ));

        if (request.isEnableWildcardSearch()) {
            textQuery.should(Query.of(q -> q
                    .wildcard(w -> w
                            .field("title.keyword")
                            .value("*" + keyword.toLowerCase() + "*")
                            .boost(0.5f)
                    )
            ));
        }

        boolQuery.must(Query.of(q -> q.bool(textQuery.build())));
    }

    /**
     * Adds filters for genres, years, ratings, and languages.
     */
    private void addFilters(BoolQuery.Builder boolQuery, AdvancedSearchRequest request) {
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                    .terms(t -> t
                            .field("genre.keyword")
                            .terms(TermsQueryField.of(tf -> tf.value(
                                    request.getGenres().stream()
                                            .map(FieldValue::of)
                                            .collect(Collectors.toList())
                            )))
                    )
            ));
        }

        if (request.getFromYear() != null || request.getToYear() != null) {
            NumberRangeQuery.Builder yearRange = new NumberRangeQuery.Builder().field("year");
            if (request.getFromYear() != null) yearRange.gte((double) request.getFromYear());
            if (request.getToYear() != null) yearRange.lte((double) request.getToYear());
            boolQuery.filter(Query.of(q -> q.range(r -> r.number(yearRange.build()))));
        }

        if (request.getRating() != null) {
            boolQuery.filter(Query.of(q -> q
                    .term(t -> t.field("rating.keyword").value(request.getRating()))
            ));
        }

        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                    .terms(t -> t
                            .field("language.keyword")
                            .terms(TermsQueryField.of(tf -> tf.value(
                                    request.getLanguages().stream()
                                            .map(FieldValue::of)
                                            .collect(Collectors.toList())
                            )))
                    )
            ));
        }
    }

    /**
     * Adds range queries for numeric fields like score and duration.
     */
    private void addRangeQueries(BoolQuery.Builder boolQuery, AdvancedSearchRequest request) {
        if (request.getMinScore() != null || request.getMaxScore() != null) {
            NumberRangeQuery.Builder scoreRange = new NumberRangeQuery.Builder().field("score");
            if (request.getMinScore() != null) scoreRange.gte(request.getMinScore());
            if (request.getMaxScore() != null) scoreRange.lte(request.getMaxScore());
            boolQuery.filter(Query.of(q -> q.range(r -> r.number(scoreRange.build()))));
        }

        if (request.getMinDuration() != null || request.getMaxDuration() != null) {
            NumberRangeQuery.Builder durationRange = new NumberRangeQuery.Builder().field("duration");
            if (request.getMinDuration() != null) durationRange.gte((double) request.getMinDuration());
            if (request.getMaxDuration() != null) durationRange.lte((double) request.getMaxDuration());
            boolQuery.filter(Query.of(q -> q.range(r -> r.number(durationRange.build()))));
        }
    }


    /**
     * Adds geospatial search for location-based filtering.
     */
    private void addGeospatialSearch(BoolQuery.Builder boolQuery, AdvancedSearchRequest request) {
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadius() != null) {
            boolQuery.filter(Query.of(q -> q
                    .geoDistance(g -> g
                            .field("filming_location")
                            .distance(request.getRadius() + "km")
                            .location(l -> l.latlon(ll -> ll
                                    .lat(request.getLatitude())
                                    .lon(request.getLongitude())
                            ))
                    )
            ));
        }
    }

    /**
     * Personalizes the query based on user preferences.
     */
    private void personalizeQuery(BoolQuery.Builder boolQuery, AdvancedSearchRequest request) {
        if (request.getUserPreferences() != null) {
            request.getUserPreferences().getPreferredGenres().forEach(genre -> {
                boolQuery.should(Query.of(q -> q
                        .term(t -> t
                                .field("genre.keyword")
                                .value(genre)
                                .boost(1.2f)
                        )
                ));
            });
        }
    }

    /**
     * Wraps the query with function score for custom scoring.
     */
    private BoolQuery.Builder wrapWithFunctionScore(BoolQuery.Builder boolQuery, AdvancedSearchRequest request) {
        if (request.isBoostPopular()) {
            boolQuery.should(Query.of(q -> q
                    .functionScore(fs -> fs
                            .query(Query.of(qu -> qu.bool(boolQuery.build())))
                            .functions(f -> f
                                    .filter(Query.of(filter -> filter.range(r -> r
                                            .number(nr -> nr.field("popularity").gte(8.0))
                                    )))
                                    .weight(1.5)
                            )
                    )
            ));
        }
        return boolQuery;
    }

    /**
     * Adds dynamic sorting to the search request.
     */
    private void addSorting(SearchRequest.Builder searchBuilder, AdvancedSearchRequest request) {
        List<SortOptions> sortOptions = new ArrayList<>();

        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f
                            .field(request.getSortBy())
                            .order(request.isAscending() ?
                                    co.elastic.clients.elasticsearch._types.SortOrder.Asc :
                                    co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                            .missing("_last")
                    )
            ));
        }

        sortOptions.add(SortOptions.of(s -> s.score(sc -> sc.order(
                co.elastic.clients.elasticsearch._types.SortOrder.Desc
        ))));

        sortOptions.add(SortOptions.of(s -> s
                .field(f -> f
                        .field("movieId")
                        .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                )
        ));

        searchBuilder.sort(sortOptions);
    }

    /**
     * Adds advanced highlighting to the search request.
     */
    private void addHighlighting(SearchRequest.Builder searchBuilder, AdvancedSearchRequest request) {
        if (request.isEnableHighlighting()) {
            Map<String, HighlightField> highlightFields = new HashMap<>();

            highlightFields.put("title", HighlightField.of(hf -> hf
                    .preTags("<mark class='highlight'>")
                    .postTags("</mark>")
                    .fragmentSize(100)
                    .numberOfFragments(1)
            ));

            highlightFields.put("description", HighlightField.of(hf -> hf
                    .preTags("<mark class='highlight'>")
                    .postTags("</mark>")
                    .fragmentSize(150)
                    .numberOfFragments(2)
            ));

            searchBuilder.highlight(h -> h
                    .fields(highlightFields)
                    .requireFieldMatch(false)
                    .encoder(HighlighterEncoder.Html)
            );
        }
    }

    /**
     * Adds aggregations for faceted search.
     */
    private void addAggregations(SearchRequest.Builder searchBuilder, AdvancedSearchRequest request) {
        if (request.isIncludeAggregations()) {
            Map<String, Aggregation> aggregations = new HashMap<>();

            aggregations.put("genres", Aggregation.of(a -> a
                    .terms(t -> t.field("genre.keyword").size(SearchConfigUtil.MAX_AGGREGATION_SIZE))
            ));

            aggregations.put("years", Aggregation.of(a -> a
                    .histogram(h -> h.field("year").interval(5.0).extendedBounds(eb -> eb.min(1900.0).max(2030.0)))
            ));

            aggregations.put("ratings", Aggregation.of(a -> a
                    .terms(t -> t.field("rating.keyword"))
            ));

            aggregations.put("score_ranges", Aggregation.of(a -> a
                    .range(r -> r
                            .field("score")
                            .ranges(ra -> ra.to(5.0).key("low"))
                            .ranges(ra -> ra.from(5.0).to(7.0).key("medium"))
                            .ranges(ra -> ra.from(7.0).to(8.5).key("high"))
                            .ranges(ra -> ra.from(8.5).key("excellent"))
                    )
            ));

            aggregations.put("languages", Aggregation.of(a -> a
                    .terms(t -> t.field("language.keyword").size(SearchConfigUtil.MAX_AGGREGATION_SIZE))
            ));

            searchBuilder.aggregations(aggregations);
        }
    }

    /**
     * Adds search suggestions to the search request.
     */
    private void addSuggestions(SearchRequest.Builder searchBuilder, AdvancedSearchRequest request) {
        if (request.getKeyword() != null && !request.getKeyword().isEmpty() && request.isIncludeSuggestions()) {
            int suggestionSize = request.getSuggestionSize() != null ? request.getSuggestionSize() : SearchConfigUtil.MAX_SUGGESTION_SIZE;

            Suggester suggester = Suggester.of(s -> s
                    .suggesters("title_suggest", new FieldSuggester.Builder()
                            .term(t -> t
                                    .field("title")
                                    .text(request.getKeyword())
                                    .suggestMode(SuggestMode.Popular)
                                    .size(suggestionSize))
                            .build())
                    .suggesters("phrase_suggest", new FieldSuggester.Builder()
                            .phrase(p -> p
                                    .field("title.suggest")
                                    .text(request.getKeyword())
                                    .confidence(0.5)
                                    .size(suggestionSize))
                            .build())
                    .suggesters("completion_suggest", new FieldSuggester.Builder()
                            .completion(c -> c
                                    .field("title_completion")
                                    .analyzer(request.getKeyword())
                                    .size(suggestionSize))
                            .build())
            );

            searchBuilder.suggest(s -> s.suggesters(suggester.suggesters()));
        }
    }

    /**
     * Builds a comprehensive search response.
     */
    private ElasSearchResponse buildSearchResponse(SearchResponse<Movie> response, AdvancedSearchRequest request) {
        List<ElasSearchResponse.MovieResult> results = response.hits().hits().stream()
                .map(this::mapToAdvancedMovieResult)
                .collect(Collectors.toList());

        ElasSearchResponse searchResponse = new ElasSearchResponse();
        searchResponse.setResults(results);
        searchResponse.setTotalResults((int) response.hits().total().value());
        searchResponse.setPage(request.getPage());
        searchResponse.setTotalPages((int) Math.ceil((double) response.hits().total().value() / request.getSize()));

        if (request.isIncludeAggregations() && response.aggregations() != null) {
            searchResponse.setAggregations(processAggregations(response.aggregations()));
        }

        if (request.isIncludeSuggestions() && response.suggest() != null) {
            searchResponse.setSuggestions(processSuggestions(response.suggest()));
        }

        searchResponse.setAnalytics(buildSearchAnalytics(response, request));

        log.info("Advanced search executed: keyword={}, totalResults={}, took={}ms",
                request.getKeyword(), searchResponse.getTotalResults(), response.took());

        return searchResponse;
    }

    /**
     * Maps a search hit to an advanced movie result.
     */
    private ElasSearchResponse.MovieResult mapToAdvancedMovieResult(Hit<Movie> hit) {
        Movie movie = hit.source();
        ElasSearchResponse.MovieResult result = new ElasSearchResponse.MovieResult();

        result.setMovieId(movie.getMovieId());
        result.setTitle(movie.getTitle());
        result.setDescription(movie.getDescription());
        result.setGenre(movie.getGenre());
        result.setScore(hit.score());
        result.setRelevanceScore(hit.score());
        result.setIndex(hit.index());
        result.setVersion(hit.version());

        if (hit.highlight() != null) {
            Map<String, List<String>> highlights = new HashMap<>();
            hit.highlight().forEach((field, fragments) -> {
                highlights.put(field, new ArrayList<>(fragments));
            });
            result.setHighlights(highlights);
        }

        if (hit.innerHits() != null) {
            result.setInnerHits(processInnerHits(hit.innerHits()));
        }

        return result;
    }

    /**
     * Processes aggregation results.
     */
    private Map<String, Object> processAggregations(Map<String, Aggregate> aggregations) {
        Map<String, Object> processedAggs = new HashMap<>();

        aggregations.forEach((name, agg) -> {
            if (agg.isSterms()) {
                processedAggs.put(name, processTermsAggregation(agg.sterms()));
            } else if (agg.isHistogram()) {
                processedAggs.put(name, processHistogramAggregation(agg.histogram()));
            } else if (agg.isRange()) {
                processedAggs.put(name, processRangeAggregation(agg.range()));
            }
        });

        return processedAggs;
    }

    private List<Map<String, Object>> processTermsAggregation(StringTermsAggregate terms) {
        return terms.buckets().array().stream()
                .map(bucket -> {
                    Map<String, Object> bucketMap = new HashMap<>();
                    bucketMap.put("key", bucket.key().stringValue());
                    bucketMap.put("docCount", bucket.docCount());
                    return bucketMap;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> processHistogramAggregation(HistogramAggregate histogram) {
        return histogram.buckets().array().stream()
                .map(bucket -> {
                    Map<String, Object> bucketMap = new HashMap<>();
                    bucketMap.put("key", bucket.key());
                    bucketMap.put("docCount", bucket.docCount());
                    return bucketMap;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> processRangeAggregation(RangeAggregate range) {
        return range.buckets().array().stream()
                .map(bucket -> {
                    Map<String, Object> bucketMap = new HashMap<>();
                    bucketMap.put("key", bucket.key());
                    bucketMap.put("from", bucket.from());
                    bucketMap.put("to", bucket.to());
                    bucketMap.put("docCount", bucket.docCount());
                    return bucketMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Processes suggestion results.
     */
    private List<SearchSuggestion> processSuggestions(Map<String, List<Suggestion<Movie>>> suggestions) {
        List<SearchSuggestion> processedSuggestions = new ArrayList<>();

        suggestions.forEach((name, suggestionList) -> {
            suggestionList.forEach(suggestion -> {
                if (suggestion.isTerm()) {
                    suggestion.term().options().forEach(option -> {
                        SearchSuggestion searchSuggestion = new SearchSuggestion();
                        searchSuggestion.setType("term");
                        searchSuggestion.setText(option.text());
                        searchSuggestion.setScore(option.score());
                        searchSuggestion.setFreq(option.freq());
                        processedSuggestions.add(searchSuggestion);
                    });
                } else if (suggestion.isPhrase()) {
                    suggestion.phrase().options().forEach(option -> {
                        SearchSuggestion searchSuggestion = new SearchSuggestion();
                        searchSuggestion.setType("phrase");
                        searchSuggestion.setText(option.text());
                        searchSuggestion.setScore(option.score());
                        processedSuggestions.add(searchSuggestion);
                    });
                } else if (suggestion.isCompletion()) {
                    suggestion.completion().options().forEach(option -> {
                        SearchSuggestion searchSuggestion = new SearchSuggestion();
                        searchSuggestion.setType("completion");
                        searchSuggestion.setText(option.text());
                        searchSuggestion.setScore(option.score());
                        processedSuggestions.add(searchSuggestion);
                    });
                }
            });
        });

        return processedSuggestions;
    }

    /**
     * Processes inner hits for nested objects.
     */
    private Map<String, Object> processInnerHits(Map<String, InnerHitsResult> innerHits) {
        Map<String, Object> processedInnerHits = new HashMap<>();

        innerHits.forEach((name, hits) -> {
            List<Map<String, Object>> hitsList = hits.hits().hits().stream()
                    .map(hit -> {
                        Map<String, Object> hitMap = new HashMap<>();
                        hitMap.put("source", hit.source());
                        hitMap.put("score", hit.score());
                        return hitMap;
                    })
                    .collect(Collectors.toList());
            processedInnerHits.put(name, hitsList);
        });

        return processedInnerHits;
    }

    /**
     * Builds search analytics.
     */
    private SearchAnalytics buildSearchAnalytics(SearchResponse<Movie> response, AdvancedSearchRequest request) {
        SearchAnalytics analytics = new SearchAnalytics();
        analytics.setTotalHits(response.hits().total().value());
        analytics.setMaxScore(response.hits().maxScore());
        analytics.setTookMillis(response.took());
        analytics.setTimedOut(response.timedOut());
        analytics.setSearchTime(LocalDateTime.now());
        analytics.setQuery(request.getKeyword());

        Map<String, Long> genreDistribution = response.hits().hits().stream()
                .collect(Collectors.groupingBy(
                        hit -> hit.source().getGenre(),
                        Collectors.counting()
                ));
        analytics.setGenreDistribution(genreDistribution);

        return analytics;
    }

    /**
     * Performs a More Like This search.
     *
     * @param movieId the ID of the movie to find similar movies for
     * @param size    the number of results to return
     * @return an ElasSearchResponse containing similar movies
     */
    public ElasSearchResponse moreLikeThis(String movieId, int size) throws IOException {
        Timer timer = Timer.builder("elasticsearch.search")
                .tag("type", "moreLikeThis")
                .register(meterRegistry);
        return timer.record(() -> {
            try {
                SearchResponse<Movie> response = elasticsearchClient.search(s -> s
                                .index("movies")
                                .query(q -> q
                                        .moreLikeThis(mlt -> mlt
                                                .fields(List.of("title", "description", "genre"))
                                                .like(Like.of(l -> l.document(d -> d.index("movies").id(movieId))))
                                                .minTermFreq(1)
                                                .minDocFreq(1)
                                                .maxQueryTerms(25)
                                                .boost(1.0f)
                                        )
                                )
                                .size(size),
                        Movie.class
                );

                List<ElasSearchResponse.MovieResult> results = response.hits().hits().stream()
                        .map(this::mapToAdvancedMovieResult)
                        .collect(Collectors.toList());

                ElasSearchResponse searchResponse = new ElasSearchResponse();
                searchResponse.setResults(results);
                searchResponse.setTotalResults((int) response.hits().total().value());

                return searchResponse;
            } catch (IOException e) {
                meterRegistry.counter("elasticsearch.search.errors", "type", "moreLikeThis").increment();
                throw new ElasticsearchSearchException("More Like This search failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Performs a scroll search for large result sets.
     *
     * @param request       the search request
     * @param scrollId      the scroll ID for continuing a scroll
     * @param scrollTimeout the scroll timeout duration
     * @return a ScrollSearchResult containing results and scroll metadata
     */
    public ScrollSearchResult scrollSearch(AdvancedSearchRequest request, String scrollId, String scrollTimeout) throws IOException {
        Timer timer = Timer.builder("elasticsearch.search")
                .tag("type", "scroll")
                .register(meterRegistry);
        return timer.record(() -> {
            try {
                if (scrollId == null) {
                    validateRequest(request);
                    BoolQuery.Builder query = buildAdvancedQuery(request);

                    SearchResponse<Movie> response = elasticsearchClient.search(s -> s
                                    .index("movies")
                                    .query(q -> q.bool(query.build()))
                                    .size(request.getSize())
                                    .scroll(Time.of(t -> t.time(scrollTimeout != null ? scrollTimeout : SearchConfigUtil.DEFAULT_SCROLL_TIMEOUT))),
                            Movie.class
                    );

                    List<ElasSearchResponse.MovieResult> results = response.hits().hits().stream()
                            .map(this::mapToAdvancedMovieResult)
                            .collect(Collectors.toList());

                    ScrollSearchResult result = new ScrollSearchResult(response.scrollId(), results, response.hits().total().value());
                    if (result.shouldClearScroll()) {
                        clearScroll(result.getScrollId());
                    }
                    return result;
                } else {
                    ScrollResponse<Movie> response = elasticsearchClient.scroll(s -> s
                                    .scrollId(scrollId)
                                    .scroll(Time.of(t -> t.time(scrollTimeout != null ? scrollTimeout : SearchConfigUtil.DEFAULT_SCROLL_TIMEOUT))),
                            Movie.class
                    );

                    List<ElasSearchResponse.MovieResult> results = response.hits().hits().stream()
                            .map(this::mapToAdvancedMovieResult)
                            .collect(Collectors.toList());

                    ScrollSearchResult result = new ScrollSearchResult(response.scrollId(), results, response.hits().total().value());
                    if (result.shouldClearScroll()) {
                        clearScroll(result.getScrollId());
                    }
                    return result;
                }
            } catch (IOException e) {
                meterRegistry.counter("elasticsearch.search.errors", "type", "scroll").increment();
                throw new ElasticsearchSearchException("Scroll search failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Clears a scroll ID to free up resources.
     *
     * @param scrollId the scroll ID to clear
     */
    public void clearScroll(String scrollId) throws IOException {
        if (scrollId != null) {
            try {
                elasticsearchClient.clearScroll(c -> c.scrollId(List.of(scrollId)));
                log.info("Cleared scroll ID: {}", scrollId);
            } catch (IOException e) {
                log.warn("Failed to clear scroll ID {}: {}", scrollId, e.getMessage());
            }
        }
    }
}