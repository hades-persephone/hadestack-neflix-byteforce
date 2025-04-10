package io.watch.notification.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.notification.dto.MovieNotificationDTO;
import io.watch.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "movie-notifications", groupId = "notification-group")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            MovieNotificationDTO dto = objectMapper.readValue(record.value(), MovieNotificationDTO.class);
            log.info("📥 Received notification: {} - {}", dto.getType(), dto.getMessage());
            notificationService.handleNotification(dto);
        } catch (Exception ex) {
            log.error("❌ Error processing Kafka message: {}", ex.getMessage());
        }
    }
}
