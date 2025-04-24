package io.watch.recommendation.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.watch.grpc.movie.GetMovieRequest;
import io.watch.grpc.movie.MovieResponse;
import io.watch.grpc.movie.MovieServiceGrpc;
import io.watch.grpc.user.GetProfilesRequest;
import io.watch.grpc.user.ProfileResponse;
import io.watch.grpc.user.UserServiceGrpc;
import io.watch.recommendation.model.Recommendation;
import io.watch.recommendation.model.UserBehavior;
import io.watch.recommendation.repository.RecommendationRepository;
import io.watch.recommendation.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    private final UserBehaviorRepository behaviorRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    private final MovieServiceGrpc.MovieServiceBlockingStub movieServiceStub;
    private final KafkaTemplate<String, UserBehavior> kafkaTemplate;


    private static final String USER_BEHAVIORS_TOPIC = "user-behaviors";
    private static final String FAILED_BEHAVIORS_TOPIC = "failed-behaviors";

    @Retry(name = "userService")
    @CircuitBreaker(name = "userService", fallbackMethod = "recordBehaviorFallback")
    public void recordBehavior(Long userId, Long profileId, Long movieId, String eventType, Long timestamp) {
        userServiceStub.getUserProfiles(GetProfilesRequest.newBuilder()
                        .setUserId(userId)
                        .build())
                .getProfilesList()
                .stream()
                .filter(p -> p.getProfileId() == profileId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid profile ID"));

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProfileId(profileId);
        behavior.setMovieId(movieId);
        behavior.setEventType(eventType);
        behavior.setTimestamp(timestamp);
        behaviorRepository.save(behavior);
        kafkaTemplate.send(USER_BEHAVIORS_TOPIC, userId.toString(), behavior);
        log.info("Published behavior to Kafka: userId={}, profileId={}", userId, profileId);
    }

    private void recordBehaviorFallback(Long userId, Long profileId, Long movieId, String eventType, Long timestamp, Throwable t) {
        log.error("Circuit breaker fallback for recordBehavior: userId={}, profileId={}, error={}", userId, profileId, t.getMessage());
        // Queue failed behavior in Kafka
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProfileId(profileId);
        behavior.setMovieId(movieId);
        behavior.setEventType(eventType);
        behavior.setTimestamp(timestamp);
        kafkaTemplate.send(FAILED_BEHAVIORS_TOPIC, userId.toString(), behavior);
        log.info("Queued failed behavior to Kafka: userId={}, profileId={}", userId, profileId);
    }

    public List<Recommendation> getRecommendations(Long userId, Long profileId) {
        var userResponseCircuitBreaker = getUserResponseCircuitBreaker(userId, profileId);

        // Check Redis cache
        List<Recommendation> cached = recommendationRepository.find(userId, profileId);
        if (cached != null) {
            return cached;
        }

        List<Recommendation> recommendations = new ArrayList<>();
        if(userResponseCircuitBreaker != null) {
            List<UserBehavior> behaviors = behaviorRepository.findByUserId(userId);
            for (UserBehavior behavior : behaviors) {
                if ("play".equals(behavior.getEventType())) {
                    var movie = getUserResponseGrpc(behavior.getMovieId());

                    Recommendation recommendation = new Recommendation();
                    recommendation.setMovieId(behavior.getMovieId());
                    recommendation.setTitle(movie.getTitle());
                    recommendation.setScore(0.9);
                    recommendations.add(recommendation);
                }
            }

            recommendationRepository.save(userId, profileId, recommendations);
        }
        return recommendations;
    }

    @CircuitBreaker(name = "movieService", fallbackMethod = "getRecommendationsMovieServiceFallback")
    private MovieResponse getUserResponseGrpc(Long movieId) {
        return movieServiceStub.getMovie(GetMovieRequest.newBuilder()
                .setMovieId(movieId)
                .build());
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getRecommendationsUserServiceFallback")
    private ProfileResponse getUserResponseCircuitBreaker(Long userId, Long profileId) {
        return userServiceStub.getUserProfiles(GetProfilesRequest.newBuilder()
                        .setUserId(userId)
                        .build())
                .getProfilesList()
                .stream()
                .filter(p -> p.getProfileId() == profileId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid profile ID"));
    }

    private List<Recommendation> getRecommendationsUserServiceFallback(Long userId, Long profileId, Throwable t) {
        log.error("Circuit breaker fallback for getRecommendations (userService): userId={}, profileId={}, error={}",
                userId, profileId, t.getMessage());
        List<Recommendation> cached = recommendationRepository.find(userId, profileId);
        if (cached != null) {
            log.info("Returning cached recommendations for userId={}, profileId={}", userId, profileId);
            return cached;
        }
        throw new RuntimeException("Failed to validate profile due to user-service unavailability: " + t.getMessage());
    }

    private List<Recommendation> getRecommendationsMovieServiceFallback(Long userId, Long profileId, Throwable t) {
        log.error("Circuit breaker fallback for getRecommendations (movieService): userId={}, profileId={}, error={}",
                userId, profileId, t.getMessage());
        List<UserBehavior> behaviors = behaviorRepository.findByUserId(userId);
        List<Recommendation> recommendations = new ArrayList<>();
        for (UserBehavior behavior : behaviors) {
            if ("play".equals(behavior.getEventType())) {
                Recommendation recommendation = new Recommendation();
                recommendation.setMovieId(behavior.getMovieId());
                recommendation.setTitle("Unknown Title");
                recommendation.setScore(0.9);
                recommendations.add(recommendation);
            }
        }
        recommendationRepository.save(userId, profileId, recommendations);
        return recommendations;
    }
}
