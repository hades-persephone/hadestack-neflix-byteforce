package io.watch.recommendation.config;

import io.watch.recommendation.model.UserBehavior;
import io.watch.recommendation.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BehaviorCassandraConsumer {


    private final UserBehaviorRepository behaviorRepository;

    private static final String USER_BEHAVIORS_TOPIC = "user-behaviors";
    private static final String RECOMMENDATION_CASSANDRA = "recommendation-cassandra";

    @KafkaListener(topics = USER_BEHAVIORS_TOPIC, groupId = RECOMMENDATION_CASSANDRA)
    public void saveToCassandra(UserBehavior behavior) {
        try {
            behaviorRepository.save(behavior);
            log.info("Saved behavior to Cassandra: userId={}, profileId={}, movieId={}",
                    behavior.getUserId(), behavior.getProfileId(), behavior.getMovieId());
        } catch (Exception e) {
            log.error("Failed to save behavior to Cassandra: userId={}, profileId={}, error={}",
                    behavior.getUserId(), behavior.getProfileId(), e.getMessage());
        }
    }
}
