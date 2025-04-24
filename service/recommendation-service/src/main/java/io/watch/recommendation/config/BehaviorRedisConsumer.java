package io.watch.recommendation.config;

import io.watch.recommendation.model.UserBehavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class BehaviorRedisConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_BEHAVIORS_TOPIC = "user-behaviors";
    private static final String BEHAVIORS_COUNT_KEY_PREFIX = "behaviors:count:";
    private final String RECOMMENDATION_REDIS = "recommendation-redis";

    @KafkaListener(topics = USER_BEHAVIORS_TOPIC, groupId = RECOMMENDATION_REDIS, containerFactory = "redisKafkaListenerContainerFactory")
    public void updateRedisCache(UserBehavior behavior) {
        try {
            String key = BEHAVIORS_COUNT_KEY_PREFIX + behavior.getUserId() + ":" +
                        behavior.getProfileId() + ":" + behavior.getMovieId();
            if("play".equalsIgnoreCase(behavior.getEventType())) {
                redisTemplate.opsForValue().increment(key);
                redisTemplate.expire(key, 24, TimeUnit.HOURS);
                log.info("Updated Redis cache for behavior: userId={}, profileId={}, movieId={}",
                        behavior.getUserId(), behavior.getProfileId(), behavior.getMovieId());
            }
        } catch (Exception e) {
            log.error("Failed to update Redis cache for behavior: userId={}, profileId={}, error={}",
                    behavior.getUserId(), behavior.getProfileId(), e.getMessage());
            throw new RuntimeException("Redis update failed", e);
        }
    }

}
