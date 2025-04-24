package io.watch.recommendation.repository;

import io.watch.recommendation.model.Recommendation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RecommendationRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "recommendations:";

    public RecommendationRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Long userId, Long profileId, List<Recommendation> recommendations) {
        String key = KEY_PREFIX + userId + ":" + profileId;
        redisTemplate.opsForValue().set(key, recommendations, 1, TimeUnit.HOURS); // Cache for 1 hour
    }

    public List<Recommendation> find(Long userId, Long profileId) {
        String key = KEY_PREFIX + userId + ":" + profileId;
        return (List<Recommendation>) redisTemplate.opsForValue().get(key);
    }
}
