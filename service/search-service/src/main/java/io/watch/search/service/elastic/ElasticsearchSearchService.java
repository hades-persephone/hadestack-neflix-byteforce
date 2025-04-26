package io.watch.search.service.elastic;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.util.ObjectBuilder;
import io.watch.search.model.dto.ElasSearchResponse;
import io.watch.search.model.entity.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;


    public ElasSearchResponse searchMovies(String keyword, String genre, Double minScore, Double maxScore,
                                       int page, int size, String sortBy, boolean ascending) throws IOException {
        try {
            // Build boolean query
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // Keyword search with multi-match
            if (keyword != null && !keyword.isEmpty()) {
                boolQuery.must(Query.of(q -> q
                        .multiMatch(m -> m
                                .fields(List.of("title^2.0", "description^1.0", "genre^0.8"))
                                .query(keyword)
                                .type(TextQueryType.BestFields)
                        )
                ));
            }

            // Genre filter
            if (genre != null && !genre.isEmpty()) {
                boolQuery.filter(Query.of(q -> q
                        .term(t -> t
                                .field("genre.keyword")
                                .value(genre)
                        )
                ));
            }

            if (minScore != null || maxScore != null) {
                NumberRangeQuery.Builder scoreRange = new NumberRangeQuery.Builder();
                scoreRange.field("score");
                if (minScore != null) {
                    scoreRange.gte(minScore);
                }
                if (maxScore != null) {
                    scoreRange.lte(maxScore);
                }
                boolQuery.filter(Query.of(q -> q.range((Function<RangeQuery.Builder, ObjectBuilder<RangeQuery>>) scoreRange.build())));

            // Highlighting
            Highlight highlight = Highlight.of(h -> h
                    .fields("title", hf -> hf.preTags("<strong>").postTags("</strong>"))
                    .fields("description", hf -> hf.preTags("<strong>").postTags("</strong>"))
            );

            // Build search query with pagination and sorting
            SearchResponse<Movie> searchResponse = elasticsearchClient.search(s -> s
                            .index("movies")
                            .query(q -> q.bool(boolQuery.build()))
                            .highlight(highlight)
                            .from(page * size)
                            .size(size)
                            .sort(so -> so
                                    .field(f -> f
                                            .field(sortBy)
                                            .order(ascending ? co.elastic.clients.elasticsearch._types.SortOrder.Asc : co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                                    )
                            ),
                    Movie.class
            );

            // Map results to SearchResponse
            List<ElasSearchResponse.MovieResult> results = searchResponse.hits().hits().stream()
                    .map(hit -> {
                        Movie movie = hit.source();
                        ElasSearchResponse.MovieResult result = new ElasSearchResponse.MovieResult();
                        result.setMovieId(movie.getMovieId());
                        result.setTitle(movie.getTitle());
                        result.setDescription(movie.getDescription());
                        result.setGenre(movie.getGenre());
                        result.setScore(hit.score());

                        // Set highlighted fields
                        if (hit.highlight() != null) {
                            if (hit.highlight().containsKey("title")) {
                                result.setHighlightTitle(hit.highlight().get("title").get(0));
                            }
                            if (hit.highlight().containsKey("description")) {
                                result.setHighlightDescription(hit.highlight().get("description").get(0));
                            }
                        }

                        return result;
                    })
                    .collect(Collectors.toList());

            // Build response
            ElasSearchResponse response = new ElasSearchResponse();
            response.setResults(results);
            response.setTotalResults((int) searchResponse.hits().total().value());
            response.setPage(page);
            response.setTotalPages((int) Math.ceil((double) searchResponse.hits().total().value() / size));

            log.info("Executed search: keyword={}, genre={}, page={}, size={}, totalResults={}",
                    keyword, genre, page, size, response.getTotalResults());
            return response;
}
        } catch (Exception e) {
            log.error("Failed to execute search: keyword={}, genre={}, error={}", keyword, genre, e.getMessage());
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
        return null;
    }


    private ElasSearchResponse.MovieResult mapToMovieResult(SearchHit<Movie> hit) {
        Movie movie = hit.getContent();
        ElasSearchResponse.MovieResult result = new ElasSearchResponse.MovieResult();
        result.setMovieId(movie.getMovieId());
        result.setTitle(movie.getTitle());
        result.setDescription(movie.getDescription());
        result.setGenre(movie.getGenre());
        result.setScore((double) hit.getScore());
        return result;
    }
}