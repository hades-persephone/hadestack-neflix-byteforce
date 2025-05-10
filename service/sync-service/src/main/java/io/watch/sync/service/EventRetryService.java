package io.watch.sync.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.sync.entity.FailedEvent;
import io.watch.sync.repository.FailedEventRepository;
import io.watch.sync.util.MetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRetryService {

    private final FailedEventRepository failedEventRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final ObjectMapper objectMapper;
//    private final NotificationService notificationService;
    private final MetricsCollector metricsCollector;

    @Value("${cdc.retry.max-attempts:5}")
    private int maxRetryAttempts;

    @Value("${cdc.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    @Value("${cdc.retry.backoff-multiplier:2}")
    private int backoffMultiplier;

    @Value("${cdc.topics.listen}")
    private String cdcTopic;

    @Value("${cdc.dlt.topic:cdc-dead-letter}")
    private String deadLetterTopic;

    /**
     * Scheduled job to retry failed events
     */
    @Scheduled(fixedDelayString = "${cdc.retry.scheduler.interval-ms:300000}")
    @Transactional
    public void retryFailedEvents() {
        log.info("Starting scheduled retry of failed events");

        try {
            // Get events ready for retry based on retry count and last retry time
            List<FailedEvent> eventsToRetry = findEventsReadyForRetry();
            log.info("Found {} events to retry", eventsToRetry.size());

            for (FailedEvent event : eventsToRetry) {
                try {
                    // Mark as being retried
                    event.setStatus("RETRYING");
                    event.setLastRetryAt(LocalDateTime.now());
                    event.setRetryCount(event.getRetryCount() + 1);
                    failedEventRepository.save(event);

                    // Convert stored JSON back to Map
                    Map<String, Object> eventData = objectMapper.readValue(
                            event.getEventData(),
                            new TypeReference<Map<String, Object>>() {}
                    );

                    // Send back to original topic for processing
                    kafkaTemplate.send(cdcTopic, eventData).get();

                    log.info("Successfully re-published event id: {} for retry attempt: {}",
                            event.getId(), event.getRetryCount());

                    // Update metrics
                    metricsCollector.incrementEventRetries(event.getTableName(), event.getOperation());

                } catch (Exception e) {
                    log.error("Failed to retry event id: {}", event.getId(), e);

                    // Check if max retries reached
                    if (event.getRetryCount() >= maxRetryAttempts) {
                        handleMaxRetriesReached(event);
                    } else {
                        // Mark for future retry
                        event.setStatus("PENDING");
                        failedEventRepository.save(event);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during failed events retry process", e);
//            notificationService.sendAlert("CDC Retry Failure",
//                    "Error occurred during scheduled retry of failed events: " + e.getMessage());
        }
    }

    /**
     * Find events that are ready for retry based on exponential backoff strategy
     */
    private List<FailedEvent> findEventsReadyForRetry() {
        LocalDateTime now = LocalDateTime.now();

        return failedEventRepository.findByStatusAndRetryCountLessThan("PENDING", maxRetryAttempts).stream()
                .filter(event -> {
                    if (event.getLastRetryAt() == null) {
                        // First retry attempt - apply initial delay
                        return event.getCreatedAt().plusMinutes(initialDelayMinutes).isBefore(now);
                    } else {
                        // Calculate backoff delay based on retry count
                        long delayMinutes = initialDelayMinutes * (long) Math.pow(backoffMultiplier, event.getRetryCount());
                        return event.getLastRetryAt().plusMinutes(delayMinutes).isBefore(now);
                    }
                })
                .toList();
    }

    /**
     * Handle events that have reached maximum retry attempts
     */
    private void handleMaxRetriesReached(FailedEvent event) {
        try {
            log.warn("Max retry attempts ({}) reached for event ID: {}", maxRetryAttempts, event.getId());

            // Convert stored JSON back to Map
            Map<String, Object> eventData = objectMapper.readValue(
                    event.getEventData(),
                    new TypeReference<Map<String, Object>>() {}
            );

            // Send to dead letter topic
            kafkaTemplate.send(deadLetterTopic, eventData).get();

            // Update status
            event.setStatus("FAILED");
            failedEventRepository.save(event);

            // Send notification
//            notificationService.sendAlert("CDC Event Failed Permanently",
//                    String.format("Event ID: %d for table %s with operation %s has failed after %d retry attempts",
//                            event.getId(), event.getTableName(), event.getOperation(), maxRetryAttempts));

            // Update metrics
            metricsCollector.incrementFailedEvents(event.getTableName(), event.getOperation());

        } catch (Exception e) {
            log.error("Failed to process max-retries-reached event: {}", event.getId(), e);
        }
    }

    /**
     * Manual method to retry specific failed events by IDs
     */
    @Transactional
    public void manualRetry(List<Long> eventIds) {
        List<FailedEvent> events = failedEventRepository.findAllById(eventIds);

        for (FailedEvent event : events) {
            try {
                // Reset retry count for manual intervention
                event.setRetryCount(0);
                event.setStatus("PENDING");
                failedEventRepository.save(event);

                log.info("Marked event ID: {} for manual retry", event.getId());
            } catch (Exception e) {
                log.error("Failed to queue manual retry for event ID: {}", event.getId(), e);
            }
        }
    }
}