package io.watch.history.handler;

import io.watch.history.dto.FailedActivityRecord;
import io.watch.history.service.ActionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class FailedActivityRetryJob {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ActionHistoryService userActivityService;

    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void retryFailedActivities() {
        try {
            Set<String> failedKeys = redisTemplate.keys("failed_activities:*");

            for (String key : failedKeys) {
                FailedActivityRecord failedRecord =
                        (FailedActivityRecord) redisTemplate.opsForValue().get(key);

                if (failedRecord != null && failedRecord.getRetryCount() < 3) {
                    try {
                        userActivityService.saveUserActivityWithRetry(failedRecord.getActivity()).get();
                        redisTemplate.delete(key);
                        log.info("Successfully retried failed activity: {}", key);
                    } catch (Exception e) {
                        // Increment retry count
                        failedRecord.setRetryCount(failedRecord.getRetryCount() + 1);
                        redisTemplate.opsForValue().set(key, failedRecord, Duration.ofHours(24));
                        log.warn("Retry failed for activity: {}, attempt: {}",
                                key, failedRecord.getRetryCount());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in retry job", e);
        }
    }
}