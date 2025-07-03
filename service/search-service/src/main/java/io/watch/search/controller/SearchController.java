package io.watch.search.controller;

import io.watch.search.exception.ElasticsearchSearchException;
import io.watch.search.model.dto.AdvancedSearchRequest;
import io.watch.search.model.dto.ElasSearchResponse;
import io.watch.search.model.dto.ScrollSearchResult;
import io.watch.search.service.elastic.ElasticsearchSearchService;
import io.watch.search.service.impl.SearchServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchServiceImpl searchService;
    private final ElasticsearchSearchService service;


    /**
     * Performs an advanced synchronous movie search.
     *
     * @param request the advanced search request
     * @return a response containing search results and metadata
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Mono<ElasSearchResponse>>> advancedSearch(@Valid @RequestBody AdvancedSearchRequest request) {
        try {
            log.info("Received advanced search request: keyword={}, page={}, size={}",
                    request.getKeyword(), request.getPage(), request.getSize());
            Mono<ElasSearchResponse> response = searchService.searchMovies(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Search completed successfully", response));
        } catch (ElasticsearchSearchException e) {
            log.error("Search failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
        }
    }

    /**
     * Performs an advanced asynchronous movie search.
     *
     * @param request the advanced search request
     * @return a CompletableFuture with the search response
     */
    @PostMapping("/search/async")
    public CompletableFuture<ResponseEntity<ApiResponse<ElasSearchResponse>>> advancedSearchAsync(@Valid @RequestBody AdvancedSearchRequest request) {
        log.info("Received async search request: keyword={}, page={}, size={}",
                request.getKeyword(), request.getPage(), request.getSize());
        return service.advancedSearchMoviesAsync(request)
                .thenApply(response -> ResponseEntity.ok(new ApiResponse<>(true, "Async search completed successfully", response)))
                .exceptionally(throwable -> {
                    log.error("Async search failed: {}", throwable.getMessage(), throwable);
                    String message = throwable instanceof ElasticsearchSearchException
                            ? "Async search failed: " + throwable.getMessage()
                            : "Unexpected error: " + throwable.getMessage();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse<>(false, message, null));
                });
    }

    /**
     * Performs a "more like this" search to find similar movies.
     *
     * @param movieId the ID of the movie to find similar movies for
     * @param size    the number of results to return
     * @return a response containing similar movies
     */
    @PostMapping("/more-like-this")
    public ResponseEntity<ApiResponse<ElasSearchResponse>> moreLikeThis(
            @RequestParam @NotBlank @Size(min = 1, max = 100) String movieId,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        try {
            log.info("Received more-like-this request: movieId={}, size={}", movieId, size);
            ElasSearchResponse response = service.moreLikeThis(movieId, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "More like this search completed successfully", response));
        } catch (ElasticsearchSearchException e) {
            log.error("More like this search failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "More like this search failed: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during more-like-this search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
        }
    }

    /**
     * Performs a scroll search for large result sets.
     *
     * @param request       the advanced search request
     * @param scrollId      the scroll ID for continuing a scroll (optional)
     * @param scrollTimeout the scroll timeout duration (optional, default: 1m)
     * @return a response containing scroll search results
     */
    @PostMapping("/scroll")
    public ResponseEntity<ApiResponse<ScrollSearchResult>> scrollSearch(
            @Valid @RequestBody AdvancedSearchRequest request,
            @RequestParam(required = false) String scrollId,
            @RequestParam(defaultValue = "1m") @Size(min = 1, max = 10) String scrollTimeout) {
        try {
            log.info("Received scroll search request: scrollId={}, keyword={}, page={}, size={}",
                    scrollId, request.getKeyword(), request.getPage(), request.getSize());
            ScrollSearchResult response = service.scrollSearch(request, scrollId, scrollTimeout);
            return ResponseEntity.ok(new ApiResponse<>(true, "Scroll search completed successfully", response));
        } catch (ElasticsearchSearchException e) {
            log.error("Scroll search failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Scroll search failed: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during scroll search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
        }
    }

    /**
     * Clears a scroll ID to free up Elasticsearch resources.
     *
     * @param scrollId the scroll ID to clear
     * @return a response indicating success or failure
     */
    @DeleteMapping("/scroll/{scrollId}")
    public ResponseEntity<ApiResponse<Void>> clearScroll(@PathVariable @NotBlank @Size(min = 1, max = 1000) String scrollId) {
        try {
            log.info("Received request to clear scroll ID: {}", scrollId);
            service.clearScroll(scrollId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Scroll ID cleared successfully", null));
        } catch (IOException e) {
            log.error("Failed to clear scroll ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to clear scroll ID: " + e.getMessage(), null));
        }
    }

    @Getter
    @Setter
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }

}