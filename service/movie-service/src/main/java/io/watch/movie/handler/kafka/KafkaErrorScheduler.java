package io.watch.movie.handler.kafka;

import io.watch.movie.entity.FailedMessage;
import io.watch.movie.repository.FailedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaErrorScheduler {

    private final FailedMessageRepository failedMessageRepository;
    private final KafkaErrorHandlerService kafkaErrorHandlerService;
    private final KafkaErrorProperties kafkaErrorProperties;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldFailedMessages() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(kafkaErrorProperties.getFailedMessageRetentionDays());
            List<FailedMessage> oldMessages = failedMessageRepository.findByTopicAndCreatedAtAfter("", cutoff);

            int deletedCount = 0;
            for(FailedMessage failedMessage : oldMessages) {
                if(failedMessage.getStatus() == KafkaErrorHandlerService.FailedMessageStatus.RESOLVED) {
                    failedMessageRepository.delete(failedMessage);
                    deletedCount++;
                }
            }
            log.info("Deleted {} failed messages", deletedCount);
        } catch (Exception e) {
            log.error("Failed to cleanup old failed messages", e);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void retryFailedMessages() {
        try {
            List<FailedMessage> failedMessages = failedMessageRepository.findByStatus(KafkaErrorHandlerService.FailedMessageStatus.FAILED);
            LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(10);

            List<UUID> retryIds = failedMessages.stream()
                    .filter(msg -> msg.getCreatedAt().isBefore(retryThreshold))
                    .filter(msg -> msg.getRetryCount() < kafkaErrorProperties.getMaxRetryAttempts())
                    .map(FailedMessage::getId)
                    .toList();
            if(!retryIds.isEmpty()) {
                log.info("Retrying failed messages after {} retries", retryIds.size());
                kafkaErrorHandlerService.retryFailedMessages(retryIds);
            }
        } catch (Exception e) {
            log.error("Failed to retry failed messages", e);
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyErrorDailyReport() {
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

            List<Object[]> topicsErrors = failedMessageRepository.countByTopicSince(yesterday);
            List<Object[]> errorTypeStats = failedMessageRepository.countByErrorTypeSince(yesterday);

            StringBuilder report = new StringBuilder();
            report.append("Daily Kafka Error Report - ").append(yesterday.toLocalDate()).append("\n\n");
            report.append("Errors by Topic:\n");
            topicsErrors.forEach(row ->
                    report.append("  - ").append(row[0]).append(": ").append(row[1]).append("\n"));

            report.append("\nErrors by Type:\n");
            errorTypeStats.forEach(row ->
                    report.append("  - ").append(row[0]).append(": ").append(row[1]).append("\n"));

            log.info(report.toString());
        } catch (Exception e) {
            log.error("❌ Failed to generate daily error report", e);
        }
    }
}
