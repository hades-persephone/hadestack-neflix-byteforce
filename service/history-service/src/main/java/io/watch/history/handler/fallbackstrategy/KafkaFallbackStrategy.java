package io.watch.history.handler.fallbackstrategy;

import io.watch.history.dto.BatchMetadata;
import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import io.watch.history.entity.WatchProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaFallbackStrategy implements FallbackStorageStrategy {

    private static final String ACTION_HISTORY_TOPIC = "fallback-action-history";
    private static final String USER_HISTORY_TOPIC = "fallback-user-history";
    private static final String WATCH_PROGRESS_TOPIC = "fallback-watch-progress";
    private static final String BATCH_HISTORY_TOPIC = "fallback-batch-history";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void storeActionHistory(ActionHistoryByAction activity) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    ACTION_HISTORY_TOPIC, String.valueOf(activity.getKey().getUserId()), activity
            );

            future.whenComplete((result, ex) -> {
                if(ex == null) {
                    log.debug("Sent action history to Kafka: {}", activity.getId());
                } else {
                    log.error("Failed to send action history to Kafka: {}", activity.getId(), ex);
                    throw new RuntimeException("Failed to send action history to Kafka: " + activity.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Sent action history to Kafka: {}", activity, e);
            throw new RuntimeException("Kafka fallback storage failed", e);
        }
    }

    @Override
    public void storeUserHistory(ActionHistoryByUser userActivity) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    USER_HISTORY_TOPIC, String.valueOf(userActivity.getKey().getUserId()), userActivity
            );
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sent user history to Kafka: {}", userActivity.getKey().getUserId());
                } else {
                    log.error("Failed to send user history to Kafka: {}", userActivity.getKey().getUserId(), ex);
                    throw new RuntimeException("Kafka fallback storage failed", ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to store user history in Kafka: {}", userActivity.getKey().getUserId(), e);
            throw new RuntimeException("Kafka fallback storage failed", e);
        }
    }

    @Override
    public void storeWatchProgress(WatchProgress progress) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    WATCH_PROGRESS_TOPIC,
                    String.valueOf(progress.getUserId()),
                    progress
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sent watch progress to Kafka: {}", progress.getProgressId());
                } else {
                    log.error("Failed to send watch progress to Kafka: {}", progress.getProgressId(), ex);
                    throw new RuntimeException("Kafka fallback storage failed", ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to store watch progress in Kafka: {}", progress.getProgressId(), e);
            throw new RuntimeException("Kafka fallback storage failed", e);
        }
    }

    @Override
    public void storeBatchHistory(List<ActionHistoryByAction> activities) {
        try {
            activities.forEach(this::storeActionHistory);

            BatchMetadata batch = BatchMetadata.builder()
                    .batchId(System.currentTimeMillis())
                    .size(activities.size())
                    .userIds(activities.stream().map(item -> String.valueOf(item.getKey().getUserId())).distinct().toList())
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(BATCH_HISTORY_TOPIC, "batch", batch);
            log.debug("Sent batch history to Kafka: {} items", activities.size());
        } catch (Exception e) {
            log.error("Sent activities history to Kafka: {}", activities, e);
            throw new RuntimeException("Kafka fallback storage failed", e);
        }
    }

    @Override
    public boolean supportsBulkOperations() {
        return true;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public String getStrategyName() {
        return "Kafka";
    }
}
