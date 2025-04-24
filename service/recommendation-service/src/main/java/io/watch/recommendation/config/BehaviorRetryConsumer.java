package io.watch.recommendation.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.watch.recommendation.model.UserBehavior;
import io.watch.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BehaviorRetryConsumer {
    private final RecommendationService recommendationService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private static final String FAILED_BEHAVIORS_TOPIC = "failed-behaviors";
    private static final String RECOMMENDATION_RETRY = "recommendation-retry";

    @KafkaListener(topics = FAILED_BEHAVIORS_TOPIC, groupId = RECOMMENDATION_RETRY)
    public void retryBehavior(UserBehavior behavior) {
        CircuitBreaker userServiceCircuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
        if (userServiceCircuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            log.info("Skipping retry for behavior: userId={}, profileId={} due to OPEN circuit breaker",
                    behavior.getUserId(), behavior.getProfileId());
            return;
        }

        try {
            log.info("Retrying behavior: userId={}, profileId={}", behavior.getUserId(), behavior.getProfileId());
            recommendationService.recordBehavior(
                    behavior.getUserId(),
                    behavior.getProfileId(),
                    behavior.getMovieId(),
                    behavior.getEventType(),
                    behavior.getTimestamp()
            );
            log.info("Successfully retried behavior: userId={}, profileId={}",
                    behavior.getUserId(), behavior.getProfileId());
        } catch (Exception e) {
            log.error("Failed to retry behavior: userId={}, profileId={}, error={}",
                    behavior.getUserId(), behavior.getProfileId(), e.getMessage());
            // Re-queue for later retry (Kafka will redeliver if unacknowledged)
        }
    }
}
