package io.watch.history.handler.fallbackstrategy;

import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisFallbackStrategy implements FallbackStorageStrategy {

    private static final String ACTION_HISTORY_KEY_PREFIX = "fallback:action:";
    private static final String USER_HISTORY_KEY_PREFIX = "fallback:user:";
    private static final String WATCH_PROGRESS_KEY_PREFIX = "fallback:progress:";
    private static final String BATCH_HISTORY_KEY_PREFIX = "fallback:batch:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void storeActionHistory(ActionHistoryByAction activity) {
        try {
            String key = ACTION_HISTORY_KEY_PREFIX + activity.getId();
            redisTemplate.opsForValue().set(key, activity, TTL);

            String userKey = "fallback:user_action:" + activity.getKey().getUserId();
            redisTemplate.opsForList().leftPush(userKey, activity.getId());
            redisTemplate.expire(userKey, TTL);
            log.debug("Stored action history in Redis: {}", activity.getId());
        } catch (Exception e) {
            log.error("Failed to store action history in Redis: {}", activity.getId(), e);
            throw new RuntimeException("Redis fallback storage failed: ", e);
        }
    }

    @Override
    public void storeUserHistory(ActionHistoryByUser userActivity) {
        try {
            String key = USER_HISTORY_KEY_PREFIX + userActivity.getKey().getUserId() + Instant.now().toEpochMilli();
            redisTemplate.opsForValue().set(key, userActivity, TTL);

            String sortedSetKey = "fallback:user_timeline:" + userActivity.getKey().getUserId();
            redisTemplate.opsForZSet().add(sortedSetKey, key, System.currentTimeMillis());
            redisTemplate.expire(sortedSetKey, TTL);

            log.debug("Stored user history in Redis: {}", userActivity.getKey().getUserId());
        } catch (Exception e) {
            log.error("Failed to store user history in Redis: {}", userActivity.getKey().getUserId(), e);
            throw new RuntimeException("Redis fallback storage failed: ", e);
        }
    }

    @Override
    public void storeWatchProgress(WatchProgress progress) {
        try {
            String key = WATCH_PROGRESS_KEY_PREFIX + progress.getUserId();
            redisTemplate.opsForValue().set(key, progress, TTL);

            String lastestKey = "fallback:lastest_progress:" + progress.getUserId() + ":" + progress.getContentId();
            redisTemplate.opsForValue().set(lastestKey, progress, TTL);
            log.debug("Stored progress in Redis: {}", progress.getUserId());
        } catch (Exception e) {
            log.error("Failed to store watch progress in Redis: {}", progress, e);
            throw new RuntimeException("Redis fallback store failed", e);
        }
    }

    @Override
    public void storeBatchHistory(List<ActionHistoryByAction> activities) {
        try {
            String batchKey = BATCH_HISTORY_KEY_PREFIX + System.currentTimeMillis();
            redisTemplate.opsForList().leftPushAll(batchKey, activities.toArray());
            redisTemplate.expire(batchKey, TTL);

            activities.forEach(this::storeActionHistory);
            log.debug("Stored activities in Redis: {}", activities);
        } catch (Exception e) {
            log.error("Failed to store batch history in Redis: {} items", activities.size(), e);
            throw new RuntimeException("Redis fallback store failed", e);
        }
    }

    @Override
    public boolean supportsBulkOperations() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String getStrategyName() {
        return "Redis";
    }
}
