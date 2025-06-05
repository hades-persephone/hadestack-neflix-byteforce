package io.watch.history.handler;

import io.watch.history.dto.FailedActivityRecord;
import io.watch.history.entity.ActionHistoryByAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActivityFailureHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, ActionHistoryByAction> kafkaTemplate;

    public void handleFailedWrite(ActionHistoryByAction activity, Exception exception) {
        try {
            // Option 1: Send to Kafka Dead Letter Topic
            kafkaTemplate.send("user-activity-dlq", activity);
            log.info("Sent failed activity to DLQ: {}", activity.getId());

            // Option 2: Store in Redis for manual processing
            String key = "failed_activities:" + activity.getUserId() + ":" + System.currentTimeMillis();
            FailedActivityRecord failedRecord = FailedActivityRecord.builder()
                    .activity(activity)
                    .exception(exception.getMessage())
                    .timestamp(Instant.now())
                    .retryCount(0)
                    .build();

            redisTemplate.opsForValue().set(key, failedRecord, Duration.ofHours(24));

            // Option 3: Log to file for backup
            logFailedActivity(activity, exception);

        } catch (Exception e) {
            log.error("Failed to handle write failure for activity: {}", activity.getId(), e);
            // Last resort - log to file
            logFailedActivity(activity, exception);
        }
    }

    private void logFailedActivity(ActionHistoryByAction activity, Exception exception) {
        // Log structured data cho easy parsing
        log.error("FAILED_ACTIVITY|{}|{}",
                activity.getUserId(),
                exception.getMessage());
    }
}
