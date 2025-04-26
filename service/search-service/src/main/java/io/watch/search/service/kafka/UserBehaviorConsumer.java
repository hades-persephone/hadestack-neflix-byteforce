package io.watch.search.service.kafka;


import io.watch.search.model.kafka.UserBehavior;
import io.watch.search.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserBehaviorConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserBehaviorConsumer.class);
    private static final String USER_BEHAVIORS_TOPIC = "user-behaviors";

    private final RedisTemplate<String, String> redisTemplate;

    public UserBehaviorConsumer(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(
            topics = USER_BEHAVIORS_TOPIC,
            groupId = "search-behaviors",
            containerFactory = "behaviorKafkaListenerContainerFactory"
    )
    public void consumeUserBehavior(UserBehavior behavior) {
        try {
            if ("play".equals(behavior.getEventType())) {
                String key = RedisKeyUtil.getPreferenceKey(behavior.getUserId(), behavior.getProfileId());
                redisTemplate.opsForSet().add(key, behavior.getMovieId().toString());
                redisTemplate.expire(key, 24, TimeUnit.HOURS);
                logger.info("Updated user preferences in Redis: userId={}, profileId={}, movieId={}",
                        behavior.getUserId(), behavior.getProfileId(), behavior.getMovieId());
            }
        } catch (Exception e) {
            logger.error("Failed to update user preferences: userId={}, profileId={}, error={}",
                    behavior.getUserId(), behavior.getProfileId(), e.getMessage());
            throw new RuntimeException("Failed to update preferences", e); // For DLQ
        }
    }
}