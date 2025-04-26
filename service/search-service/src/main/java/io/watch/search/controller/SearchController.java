package io.watch.search.controller;

import io.watch.search.model.dto.SearchRequest;
import io.watch.search.model.dto.ElasSearchResponse;
import io.watch.search.service.impl.SearchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchServiceImpl searchService;

    @GetMapping
    public Mono<ElasSearchResponse> searchMovies(
            @RequestParam String query,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long profileId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setUserId(userId);
        request.setProfileId(profileId);
        request.setPage(page);
        request.setSize(size);
        return searchService.searchMovies(request);
    }
}