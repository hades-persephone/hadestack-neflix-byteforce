package io.watch.rating.config.graphql;

import graphql.schema.DataFetchingEnvironment;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingMetadata;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.Rating;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.exception.RatingNotFoundException;
import io.watch.rating.exception.RatingServiceException;
import io.watch.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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

    public CompletableFuture<Slice<Rating>> getAllRatings(DataFetchingEnvironment environment) {
        Integer page = environment.getArgument("page");
        Integer size = environment.getArgument("size");
        String sortBy = environment.getArgument("sortBy");
        String sortDirection = environment.getArgument("sortDirection");

        // Set defaults
        int pageNumber = page != null ? Math.max(0, page) : 0;
        int pageSize = size != null ? Math.min(Math.max(1, size), 100) : 20;
        String sort = sortBy != null ? sortBy : "createdAt";
        String direction = sortDirection != null ? sortDirection : "DESC";

        log.debug("Fetching all ratings with page: {}, size: {}", pageNumber, pageSize);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return ratingService.findAll(pageNumber, pageSize, sort, direction);
            } catch (Exception e) {
                log.error("Error fetching all ratings", e);
                throw new RatingServiceException("Failed to fetch ratings", e);
            }
        });
    }

    public CompletableFuture<Slice<Rating>> getRatingsByUserId(DataFetchingEnvironment environment) {
        String userId = environment.getArgument("userId");
        Integer page = environment.getArgument("page");
        Integer size = environment.getArgument("size");
        String sortBy = environment.getArgument("sortBy");
        String sortDirection = environment.getArgument("sortDirection");

        // Set defaults
        int pageNumber = page != null ? Math.max(0, page) : 0;
        int pageSize = size != null ? Math.min(Math.max(1, size), 100) : 20;
        String sort = sortBy != null ? sortBy : "createdAt";
        String direction = sortDirection != null ? sortDirection : "DESC";

        log.debug("Fetching ratings for user ID: {} with page: {}, size: {}", userId, pageNumber, pageSize);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID userUuid = UUID.fromString(userId);
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                return ratingService.findByUserId(userUuid, pageable, sort, direction);
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
                return ratingService.submittingRating(request);
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

    public CompletableFuture<Long> getRatingCount(DataFetchingEnvironment environment) {
        String movieId = environment.getArgument("movieId");
        log.debug("Fetching rating count for movie ID: {}", movieId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID movieUuid = UUID.fromString(movieId);
                return ratingService.getRatingCount(movieUuid);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for movie ID: {}", movieId, e);
                throw new RatingServiceException("Invalid movie ID format", e);
            } catch (Exception e) {
                log.error("Error fetching rating count for movie ID: {}", movieId, e);
                throw new RatingServiceException("Failed to fetch rating count", e);
            }
        });
    }

    public CompletableFuture<Boolean> hasUserRated(DataFetchingEnvironment environment) {
        String userId = environment.getArgument("userId");
        String movieId = environment.getArgument("movieId");
        log.debug("Checking if user {} has rated movie {}", userId, movieId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID userUuid = UUID.fromString(userId);
                UUID movieUuid = UUID.fromString(movieId);
                return ratingService.hasUserRated(userUuid, movieUuid);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for user ID: {} or movie ID: {}", userId, movieId, e);
                throw new RatingServiceException("Invalid UUID format", e);
            } catch (Exception e) {
                log.error("Error checking user rating for user {} and movie {}", userId, movieId, e);
                throw new RatingServiceException("Failed to check user rating", e);
            }
        });
    }


    private RatingCreateRequest convertToRatingCreateRequest(Object requestInput) {
        if (!(requestInput instanceof Map)) {
            throw new IllegalArgumentException("Invalid request input format");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) requestInput;

        RatingCreateRequest request = new RatingCreateRequest();

        try {
            request.setUserId(UUID.fromString((String) map.get("userId")));
            request.setMovieId(UUID.fromString((String) map.get("movieId")));
            request.setRating((Integer) map.get("rating"));
            request.setReview((String) map.get("review"));

            // Handle optional metadata
            if (map.containsKey("metadata") && map.get("metadata") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) map.get("metadata");
                request.setMetadata((RatingMetadata) metadata);
            }

            return request;
        } catch (Exception e) {
            log.error("Error converting request input to RatingCreateRequest", e);
            throw new IllegalArgumentException("Invalid request format: " + e.getMessage(), e);
        }
    }

    private void validateRatingRequest(RatingCreateRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        if (request.getRating() == null) {
            throw new IllegalArgumentException("Rating is required");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (request.getReview() != null && request.getReview().length() > 2000) {
            throw new IllegalArgumentException("Review must be less than 2000 characters");
        }
    }
}
