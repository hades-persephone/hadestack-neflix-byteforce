package io.watch.search.service.impl;


import io.watch.search.model.dto.AdvancedSearchRequest;
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
    public Mono<ElasSearchResponse> searchMovies(AdvancedSearchRequest searchRequest) {
        // Validate profile if provided
        Mono<Void> profileValidation = searchRequest.getUserId() != null && searchRequest.getProfileId() != null
                ? validateProfile(searchRequest.getUserId(), searchRequest.getProfileId())
                : Mono.empty();

        return profileValidation.then(Mono.defer(() -> {
            // Perform Elasticsearch search
            ElasSearchResponse response = null;
            try {
                response = elasticsearchSearchService.advancedSearchMovies(searchRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Personalize results if userId and profileId are provided
            if (searchRequest.getUserId() != null && searchRequest.getProfileId() != null) {
                String preferenceKey = RedisKeyUtil.getPreferenceKey(searchRequest.getUserId(), searchRequest.getProfileId());
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
                    searchRequest.getKeyword(), searchRequest.getUserId(), searchRequest.getProfileId());
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

    private Mono<ElasSearchResponse> searchMoviesFallback(AdvancedSearchRequest searchRequest) throws IOException {
        logger.error("Circuit breaker fallback for searchMovies: query={}, userId={}, error={}",
                searchRequest.getKeyword(), searchRequest.getUserId(), searchRequest.getLanguageAnalyzer());
        // Proceed without profile validation
        ElasSearchResponse response = elasticsearchSearchService.advancedSearchMovies(searchRequest);
        return Mono.just(response);
    }
}