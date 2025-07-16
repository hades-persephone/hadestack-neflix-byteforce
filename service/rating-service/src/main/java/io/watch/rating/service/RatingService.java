package io.watch.rating.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.watch.rating.dto.FraudDetectionResult;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.Rating;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.entity.RatingStatus;
import io.watch.rating.event.RatingCreatedEvent;
import io.watch.rating.event.RatingDeletedEvent;
import io.watch.rating.event.RatingUpdatedEvent;
import io.watch.rating.exception.RatingNotFoundException;
import io.watch.rating.repository.RatingRepository;
import io.watch.rating.repository.StatisticsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final StatisticsRepository statisticsRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FraudDetectionService fraudDetectionService;
    private final RatingAggregateService ratingAggregateService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RetryRegistry retryRegistry;

    private static final String RATING_CREATED_TOPIC = "rating.created";
    private static final String RATING_UPDATED_TOPIC = "rating.updated";
    private static final String RATING_DELETED_TOPIC = "rating.deleted";
    private static final String CACHE_KEY_PREFIX = "rating::";
    private static final int MAX_REVIEW_LENGTH = 2000;

    @Transactional
    public RatingResponse submittingRating(@Valid RatingCreateRequest request) {
        log.info("Submitting rating for user: {}, movie: {}", request.getUserId(), request.getMovieId());

        Retry retry = retryRegistry.retry("ratingServiceRetry");

        Supplier<RatingResponse> ratingSupplier = Retry.decorateSupplier(retry, () -> {
            try {
                validateRatingRequest(request);

                FraudDetectionResult fraudDetectionResult = fraudDetectionService.detectFraud(request);

                if(fraudDetectionResult.isFraudulent()) {
                    log.warn("Fraudulent rating detected: {}", fraudDetectionResult.getReasons());
                }

                Optional<Rating> existingRating = ratingRepository.findById(request.getMovieId());
                Rating rating;
                rating = existingRating.map(value -> updateExistingRating(value, request, fraudDetectionResult))
                        .orElseGet(() -> createNewRating(request, fraudDetectionResult));

                RatingCreatedEvent event = RatingCreatedEvent.builder().build();

                kafkaTemplate.send("", event);

                applicationEventPublisher.publishEvent(event);

                return mapToResponse(rating);
            } catch (Exception e) {
                log.error("Failed to submit rating for user: {}, movie: {}", request.getUserId(), request.getMovieId(), e);
                throw new RuntimeException("Failed to submit rating", e);
            }
        });

        return retry.executeSupplier(ratingSupplier);
    }

    private void publishRatingEvents(Rating rating, boolean isUpdate) {
        try {
            if (isUpdate) {
                // Update event is handled in updateExistingRating method
                return;
            }

            RatingCreatedEvent event = RatingCreatedEvent.builder()
                    .userId(rating.getUserId())
                    .movieId(rating.getMovieId())
                    .ratingValue(rating.getRatingValue())
                    .build();

            CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(RATING_CREATED_TOPIC, event.getMovieId().toString(), event)
                            .whenComplete((result, ex) -> {
                                if (ex == null) {
                                    log.debug("Successfully sent rating created event for rating: {}", event.getMovieId());
                                } else {
                                    log.error("Failed to send rating created event for rating: {}", event.getMovieId(), ex);
                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to publish rating created event", e);
                }
            });

            applicationEventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("Failed to publish rating events for rating: {}", rating.getId(), e);
        }
    }


    private void publishRatingUpdateEvent(Rating rating, Integer oldRating, String oldReview) {
        try {
            RatingUpdatedEvent updateEvent = RatingUpdatedEvent.builder()
                    .ratingId(rating.getId())
                    .userId(rating.getUserId())
                    .movieId(rating.getMovieId())
                    .oldRating(oldRating)
                    .newRating(rating.getRatingValue())
                    .oldReview(oldReview)
                    .newReview(rating.getReviewText())
                    .timestamp(LocalDateTime.now())
                    .build();

            CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(RATING_UPDATED_TOPIC, updateEvent.getRatingId().toString(), updateEvent)
                            .whenComplete((result, ex) -> {
                                if (ex == null) {
                                    log.debug("Successfully sent rating created event for rating: {}", updateEvent.getMovieId());
                                } else {
                                    log.error("Failed to send rating created event for rating: {}", updateEvent.getMovieId(), ex);
                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to publish rating updated event", e);
                }
            });

            applicationEventPublisher.publishEvent(updateEvent);

        } catch (Exception e) {
            log.error("Failed to publish rating update event for rating: {}", rating.getId(), e);
        }
    }

    private void validateRatingRequest(RatingCreateRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (request.getReview() != null && request.getReview().length() > MAX_REVIEW_LENGTH) {
            throw new IllegalArgumentException("Review text exceeds maximum length of " + MAX_REVIEW_LENGTH);
        }

        if (request.getUserId() == null || request.getMovieId() == null) {
            throw new IllegalArgumentException("User ID and Movie ID are required");
        }
    }

    private Rating createNewRating(@Valid RatingCreateRequest request, FraudDetectionResult fraudDetectionResult) {
        Rating rating = Rating.builder()
                .userId(request.getUserId())
                .movieId(request.getMovieId())
                .ratingValue(request.getRating())
                .reviewText(sanitizeReview(request.getReview()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(fraudDetectionResult.isRequiresReview() ? RatingStatus.PENDING_REVIEW : RatingStatus.ACTIVE)
                .fraudScore(fraudDetectionResult.getFraudScore())
                .ipAddress(request.getMetadata().getIpAddress())
                .build();

        return ratingRepository.save(rating);
    }


    private String sanitizeReview(String review) {
        if (review == null) return null;

        // HTML encoding to prevent XSS
        return HtmlUtils.htmlEscape(review.trim());
    }


    private Rating updateExistingRating(Rating rating, @Valid RatingCreateRequest request, FraudDetectionResult fraudDetectionResult) {
        Integer oldRating = rating.getRatingValue();
        String oldReview = rating.getReviewText();
        rating.setRatingValue(request.getRating());
        rating.setUpdatedAt(LocalDateTime.now());
        rating.setFraudScore(fraudDetectionResult.getFraudScore());
        rating.setStatus(fraudDetectionResult.isRequiresReview() ? RatingStatus.PENDING_REVIEW : RatingStatus.ACTIVE);

        Rating updatedRating = ratingRepository.save(rating);

        // Publish update event
        RatingUpdatedEvent updateEvent = RatingUpdatedEvent.builder()
                .ratingId(updatedRating.getId())
                .userId(updatedRating.getUserId())
                .movieId(updatedRating.getMovieId())
                .oldRating(oldRating)
                .newRating(updatedRating.getRatingValue())
                .oldReview(oldReview)
                .newReview(updatedRating.getReviewText())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("rating.updated", updateEvent);
        applicationEventPublisher.publishEvent(updateEvent);

        return updatedRating;
    }

    @Cacheable(value = "movieRatings", key = "#movieId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Slice<RatingResponse> getRatingsByMovie(UUID movieId, Pageable pageable) {
        try {
            log.debug("Fetching ratings for movie: {} with page: {}, size: {}",
                    movieId, pageable.getPageNumber(), pageable.getPageSize());

            Page<Rating> ratings = (Page<Rating>) ratingRepository.findActiveByMovieId(movieId, pageable);
            return ratings.map(this::mapToResponse);

        } catch (Exception e) {
            log.error("Failed to fetch ratings for movie: {}", movieId, e);
            throw new RuntimeException("Failed to fetch movie ratings", e);
        }
    }

    @Cacheable(value = "averageRatings", key = "#movieId")
    public Double getAverageRating(UUID movieId) {
        try {
            return ratingRepository.findAverageRatingByMovieId(movieId).orElse(0.0);
        } catch (Exception e) {
            log.error("Failed to calculate average rating for movie: {}", movieId, e);
            return 0.0; // Return safe default
        }
    }

    @Cacheable(value = "ratingStatistics", key = "#movieId")
    public RatingStatistics getRatingStatistics(UUID movieId) {
        try {
            return statisticsRepository.findByMovieId(movieId).orElse(null);
        } catch (Exception e) {
            log.error("Failed to fetch rating statistics for movie: {}", movieId, e);
            return null;
        }
    }

    @Cacheable(value = "rating", key = "#ratingId")
    public CompletableFuture<Rating> findById(UUID ratingId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Finding rating by ID: {}", ratingId);
            return ratingRepository.findById(ratingId)
                    .orElseThrow(() -> new RatingNotFoundException("Rating not found with ID: " + ratingId));
        });
    }


    @Transactional(readOnly = true)
    public Slice<Rating> findAll(int page, int size, String sortBy, String sortDirection) {
        log.debug("Finding all ratings with page: {}, size: {}, sort: {} {}",
                page, size, sortBy, sortDirection);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return ratingRepository.findAll(pageable);
    }

    public Slice<Rating> findByUserId(UUID userId, Pageable pageable, String sortBy, String sortDirection) {
        log.debug("Finding ratings for user ID: {} with pageable: {}, sort: {} {}",
                userId, pageable, sortBy, sortDirection);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return ratingRepository.findByUserId(userId, sortedPageable);
    }

    @Transactional
    @CacheEvict(value = {"movieRatings", "averageRatings", "ratingStatistics"}, allEntries = true)
    public void deleteUserRatings(UUID userId) {
        log.info("Deleting all ratings for user: {}", userId);

        try {
            Slice<Rating> userRatings = ratingRepository.findActiveByUserId(userId, Pageable.unpaged());

            userRatings.forEach(rating -> {
                try {
                    rating.setStatus(RatingStatus.REMOVED);
                    rating.setUpdatedAt(LocalDateTime.now());
                    ratingRepository.save(rating);

                    // Publish deletion event
                    publishRatingDeletionEvent(rating);

                    // Evict specific cache entries
                    evictRatingCache(rating.getMovieId());

                } catch (Exception e) {
                    log.error("Failed to delete rating: {} for user: {}", rating.getId(), userId, e);
                }
            });

            log.info("Successfully deleted {} ratings for user: {}", userRatings.getNumberOfElements(), userId);

        } catch (Exception e) {
            log.error("Failed to delete ratings for user: {}", userId, e);
            throw new RuntimeException("Failed to delete user ratings", e);
        }
    }


    private void publishRatingDeletionEvent(Rating rating) {
        try {
            RatingDeletedEvent event = RatingDeletedEvent.builder()
                    .ratingId(rating.getId())
                    .userId(rating.getUserId())
                    .movieId(rating.getMovieId())
                    .timestamp(LocalDateTime.now())
                    .reason("User requested deletion")
                    .build();

            CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(RATING_DELETED_TOPIC, event.getRatingId().toString(), event)
                            .whenComplete((result, ex) -> {
                                if (ex == null) {
                                    log.debug("Successfully sent rating created event for rating: {}", event.getRatingId());
                                } else {
                                    log.error("Failed to send rating created event for rating: {}", event.getRatingId(), ex);
                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to publish rating deleted event", e);
                }
            });

            applicationEventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("Failed to publish rating deletion event for rating: {}", rating.getId(), e);
        }
    }


    @Transactional(readOnly = true)
    public Long getRatingCount(UUID movieId) {
        log.debug("Getting rating count for movie ID: {}", movieId);
        return ratingRepository.countByMovieId(movieId);
    }

    @Transactional(readOnly = true)
    public Boolean hasUserRated(UUID userId, UUID movieId) {
        log.debug("Checking if user {} has rated movie {}", userId, movieId);
        return ratingRepository.existsByUserIdAndMovieId(userId, movieId);
    }

    @Transactional(readOnly = true)
    public Slice<Rating> findRecentRatings(int page, int size) {
        log.debug("Finding recent ratings with page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        return ratingRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Rating> findTopRatedMovies(int page, int size) {
        log.debug("Finding top rated movies with page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        return ratingRepository.findTopRatedMovies(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Rating> findRatingsByRatingValue(int ratingValue, int page, int size) {
        log.debug("Finding ratings with value: {} with page: {}, size: {}", ratingValue, page, size);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        return ratingRepository.findByRatingValue(ratingValue, pageable);
    }


    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUserId())
                .movieId(rating.getMovieId())
                .rating(rating.getRatingValue())
                .review(rating.getReviewText())
                .createdAt(rating.getCreatedAt())
                .isVerified(rating.getIsVerified())
                .status(rating.getStatus())
                .build();
    }

    private void evictRatingCache(UUID movieId) {
        try {
            redisTemplate.delete("movieRatings::" + movieId + "*");
            redisTemplate.delete("averageRatings::" + movieId);
            redisTemplate.delete("ratingStatistics::" + movieId);
        } catch (Exception e) {
            log.error("Failed to evict cache for movie: {}", movieId, e);
        }
    }

}
