package io.watch.rating.service;

import io.watch.rating.dto.FraudDetectionResult;
import io.watch.rating.dto.RatingCreateRequest;
import io.watch.rating.dto.RatingResponse;
import io.watch.rating.entity.Rating;
import io.watch.rating.entity.RatingStatistics;
import io.watch.rating.entity.RatingStatus;
import io.watch.rating.event.RatingCreatedEvent;
import io.watch.rating.event.RatingDeletedEvent;
import io.watch.rating.event.RatingUpdatedEvent;
import io.watch.rating.repository.RatingRepository;
import io.watch.rating.repository.StatisticsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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


    public RatingResponse submittingRating(@Valid RatingCreateRequest request) {
        log.info("Submitting rating for user: {}, movie: {}", request.getUserId(), request.getMovieId());
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

    @Cacheable(value = "movieRatings", key = "#movieId")
    public Slice<RatingResponse> getRatingsByMovie(UUID movieId, Pageable pageable) {
        Page<Rating> ratings = (Page<Rating>) ratingRepository.findActiveByMovieId(movieId, pageable);
        return ratings.map(this::mapToResponse);
    }

    @Cacheable(value = "averageRatings", key = "#movieId")
    public Double getAverageRating(UUID movieId) {
        return ratingRepository.findAverageRatingByMovieId(movieId).orElse(0.0);
    }

    @Cacheable(value = "ratingStatistics", key = "#movieId")
    public RatingStatistics getRatingStatistics(UUID movieId) {
        return statisticsRepository.findById(movieId).orElse(null);
    }

    public void deleteUserRatings(UUID userId) {
        log.info("Deleting all ratings for user: {}", userId);

        Page<Rating> userRatings = (Page<Rating>) ratingRepository.findActiveByUserId(userId, Pageable.unpaged());

        userRatings.forEach(rating -> {
            rating.setStatus(RatingStatus.REMOVED);
            ratingRepository.save(rating);

            // Publish deletion event
            RatingDeletedEvent event = RatingDeletedEvent.builder()
                    .ratingId(rating.getId())
                    .userId(rating.getUserId())
                    .movieId(rating.getMovieId())
                    .timestamp(LocalDateTime.now())
                    .reason("User requested deletion")
                    .build();

            kafkaTemplate.send("rating.deleted", event);
            evictRatingCache(rating.getMovieId());
        });
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
        redisTemplate.delete("productRatings::" + movieId);
        redisTemplate.delete("averageRatings::" + movieId);
        redisTemplate.delete("ratingStatistics::" + movieId);
    }

}
