package io.watch.rating.config.graphql;

import graphql.schema.DataFetchingEnvironment;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.Rating;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingDataFetcher {

    private final RatingService ratingService;

    public CompletableFuture<Rating> getRatingById(DataFetchingEnvironment environment) {
        String id = environment.getArgument("id");
        log.debug("Fetching rating with ID: {}", id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID ratingId = UUID.fromString(id);
                return ratingService.findById(ratingId).join();
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for rating ID: {}", id, e);
                throw new RatingServiceException("Invalid rating ID format", e);
            } catch (RatingNotFoundException e) {
                log.error("Rating not found with ID: {}", id, e);
                throw e;
            } catch (Exception e) {
                log.error("Error fetching rating with ID: {}", id, e);
                throw new RatingServiceException("Failed to fetch rating", e);
            }
        });
    }

    public CompletableFuture<Page<Rating>> getAllRatings(DataFetchingEnvironment environment) {
        Integer page = environment.getArgument("page");
        Integer size = environment.getArgument("size");

        // Set defaults
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? Math.min(size, 100) : 20; // Limit max size

        log.debug("Fetching all ratings with page: {}, size: {}", pageNumber, pageSize);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return ratingService.findAll(pageNumber, pageSize);
            } catch (Exception e) {
                log.error("Error fetching all ratings", e);
                throw new RatingServiceException("Failed to fetch ratings", e);
            }
        });
    }

    public CompletableFuture<Page<Rating>> getRatingsByUserId(DataFetchingEnvironment environment) {
        String userId = environment.getArgument("userId");
        Integer page = environment.getArgument("page");
        Integer size = environment.getArgument("size");

        // Set defaults
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? Math.min(size, 100) : 20;

        log.debug("Fetching ratings for user ID: {} with page: {}, size: {}", userId, pageNumber, pageSize);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID userUuid = UUID.fromString(userId);
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                return ratingService.findByUserId(userUuid, pageable);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for user ID: {}", userId, e);
                throw new RatingServiceException("Invalid user ID format", e);
            } catch (Exception e) {
                log.error("Error fetching ratings for user ID: {}", userId, e);
                throw new RatingServiceException("Failed to fetch user ratings", e);
            }
        });
    }

    public CompletableFuture<Slice<RatingResponse>> getRatingsByMovie(DataFetchingEnvironment environment) {
        String movieId = environment.getArgument("movieId");
        Integer page = environment.getArgument("page");
        Integer size = environment.getArgument("size");

        // Set defaults
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? Math.min(size, 100) : 20;

        log.debug("Fetching ratings for movie ID: {} with page: {}, size: {}", movieId, pageNumber, pageSize);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID movieUuid = UUID.fromString(movieId);
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                return ratingService.getRatingsByMovie(movieUuid, pageable);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for movie ID: {}", movieId, e);
                throw new RatingServiceException("Invalid movie ID format", e);
            } catch (Exception e) {
                log.error("Error fetching ratings for movie ID: {}", movieId, e);
                throw new RatingServiceException("Failed to fetch movie ratings", e);
            }
        });
    }

    public CompletableFuture<Double> getAverageRating(DataFetchingEnvironment environment) {
        String movieId = environment.getArgument("movieId");
        log.debug("Fetching average rating for movie ID: {}", movieId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID movieUuid = UUID.fromString(movieId);
                return ratingService.getAverageRating(movieUuid);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for movie ID: {}", movieId, e);
                throw new RatingServiceException("Invalid movie ID format", e);
            } catch (Exception e) {
                log.error("Error fetching average rating for movie ID: {}", movieId, e);
                throw new RatingServiceException("Failed to fetch average rating", e);
            }
        });
    }

    public CompletableFuture<RatingStatistics> getRatingStatistics(DataFetchingEnvironment environment) {
        String movieId = environment.getArgument("movieId");
        log.debug("Fetching rating statistics for movie ID: {}", movieId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID movieUuid = UUID.fromString(movieId);
                return ratingService.getRatingStatistics(movieUuid);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for movie ID: {}", movieId, e);
                throw new RatingServiceException("Invalid movie ID format", e);
            } catch (Exception e) {
                log.error("Error fetching rating statistics for movie ID: {}", movieId, e);
                throw new RatingServiceException("Failed to fetch rating statistics", e);
            }
        });
    }

    public CompletableFuture<RatingResponse> submitRating(DataFetchingEnvironment environment) {
        Object requestInput = environment.getArgument("request");
        log.debug("Submitting rating with request: {}", requestInput);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convert GraphQL input to RatingCreateRequest
                RatingCreateRequest request = convertToRatingCreateRequest(requestInput);
                return ratingService.submitRating(request);
            } catch (Exception e) {
                log.error("Error submitting rating", e);
                throw new RatingServiceException("Failed to submit rating", e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteUserRatings(DataFetchingEnvironment environment) {
        String userId = environment.getArgument("userId");
        log.debug("Deleting ratings for user ID: {}", userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID userUuid = UUID.fromString(userId);
                ratingService.deleteUserRatings(userUuid);
                return true;
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for user ID: {}", userId, e);
                throw new RatingServiceException("Invalid user ID format", e);
            } catch (Exception e) {
                log.error("Error deleting ratings for user ID: {}", userId, e);
                throw new RatingServiceException("Failed to delete user ratings", e);
            }
        });
    }

    private RatingCreateRequest convertToRatingCreateRequest(Object requestInput) {
        // Implementation depends on your GraphQL input structure
        // This is a simplified example
        if (requestInput instanceof java.util.Map) {
            Map<String, Object> map = new HashMap<>(requestInput);

            RatingCreateRequest request = new RatingCreateRequest();
            request.setUserId(UUID.fromString((String) map.get("userId")));
            request.setMovieId(UUID.fromString((String) map.get("movieId")));
            request.setRating((Integer) map.get("rating"));
            request.setReview((String) map.get("review"));

            // Handle metadata if present
            if (map.containsKey("metadata")) {
                // Convert metadata object
                // request.setMetadata(convertMetadata(map.get("metadata")));
            }

            return request;
        }

        throw new IllegalArgumentException("Invalid request input format");
    }
}
