package io.watch.recommendation.config;


import io.watch.recommendation.model.UserBehavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BehaviorElasticsearchConsumer {

    private static final String USER_BEHAVIORS_TOPIC = "user-behaviors";

    @KafkaListener(topics = USER_BEHAVIORS_TOPIC, groupId = "recommendation-elasticsearch")
    public void logToElasticsearch(UserBehavior behavior) {
        try {
            // Placeholder: Send to Elasticsearch
            log.info("Logged behavior to Elasticsearch: userId={}, profileId={}, movieId={}",
                    behavior.getUserId(), behavior.getProfileId(), behavior.getMovieId());
        } catch (Exception e) {
            log.error("Failed to log behavior to Elasticsearch: userId={}, profileId={}, error={}",
                    behavior.getUserId(), behavior.getProfileId(), e.getMessage());
        }
    }
}
