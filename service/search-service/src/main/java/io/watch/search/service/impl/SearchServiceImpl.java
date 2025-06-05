package io.watch.search.service.impl;


import io.watch.search.model.dto.SearchRequest;
import io.watch.search.model.dto.ElasSearchResponse;
import io.watch.search.model.entity.Profile;
import io.watch.search.service.elastic.ElasticsearchSearchService;
import io.watch.search.util.RedisKeyUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final ElasticsearchSearchService elasticsearchSearchService;
    private final WebClient userServiceWebClient;
    private final RedisTemplate<String, String> redisTemplate;

    @CircuitBreaker(name = "userService", fallbackMethod = "searchMoviesFallback")
    public Mono<ElasSearchResponse> searchMovies(SearchRequest request) {
        // Validate profile if provided
        Mono<Void> profileValidation = request.getUserId() != null && request.getProfileId() != null
                ? validateProfile(request.getUserId(), request.getProfileId())
                : Mono.empty();

        return profileValidation.then(Mono.defer(() -> {
            // Perform Elasticsearch search
            ElasSearchResponse response = null;
            try {
                response = elasticsearchSearchService.searchMovies(
                        request.getQuery(),                    // keyword
                        request.getGenre(),                   // genre
                        request.getMinScore(),                // minScore
                        request.getMaxScore(),                // maxScore
                        request.getPage() != null ? request.getPage() : 0,
                        request.getSize() != null ? request.getSize() : 10,
                        request.getSortBy() != null ? request.getSortBy() : "score",
                        request.getAscending() != null ? request.getAscending() : false
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Personalize results if userId and profileId are provided
            if (request.getUserId() != null && request.getProfileId() != null) {
                String preferenceKey = RedisKeyUtil.getPreferenceKey(request.getUserId(), request.getProfileId());
                Set<String> preferredMovieIds = redisTemplate.opsForSet().members(preferenceKey);
                if (preferredMovieIds != null) {
                    response.getResults().forEach(result -> {
                        if (preferredMovieIds.contains(String.valueOf(result.getMovieId()))) {
                            result.setScore(result.getScore() + 0.1);
                        }
                    });
                }
            }

            logger.info("Processed search query: query={}, userId={}, profileId={}",
                    request.getQuery(), request.getUserId(), request.getProfileId());
            return Mono.just(response);
        }));
    }

    private Mono<Void> validateProfile(Long userId, Long profileId) {
        return userServiceWebClient.get()
                .uri("/users/{userId}/profiles", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Profile>>() {})
                .flatMap(profiles -> profiles.stream()
                        .anyMatch(p -> p.getProfileId().equals(profileId))
                        ? Mono.empty()
                        : Mono.error(new RuntimeException("Invalid profile ID")));
    }

    private Mono<ElasSearchResponse> searchMoviesFallback(SearchRequest request, Throwable t) throws IOException {
        logger.error("Circuit breaker fallback for searchMovies: query={}, userId={}, error={}",
                request.getQuery(), request.getUserId(), t.getMessage());
        // Proceed without profile validation
        ElasSearchResponse response = elasticsearchSearchService.searchMovies(
                request.getQuery(),                    // keyword
                request.getGenre(),                   // genre
                request.getMinScore(),                // minScore
                request.getMaxScore(),                // maxScore
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                request.getSortBy() != null ? request.getSortBy() : "score",
                request.getAscending() != null ? request.getAscending() : false
        );
        return Mono.just(response);
    }
}