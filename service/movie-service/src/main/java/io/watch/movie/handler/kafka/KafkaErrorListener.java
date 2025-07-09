package io.watch.movie.handler.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.movie.handler.DlqMessage;
import io.watch.movie.handler.kafka.KafkaErrorHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaErrorListener {

    private final KafkaErrorHandlerService errorHandlerService;
    private final ObjectMapper objectMapper;
    private final KafkaErrorProperties kafkaErrorProperties;

    @KafkaListener(topics = "#{@kafkaErrorProperties.dlqSuffix}")
    public void handlerDlqMessages(ConsumerRecord<String, String> record) {
        log.info("Received DLQ message from: topic={}, key={}, offset={}",
                record.topic(), record.key(), record.offset());
        try {
            DlqMessage dlqMessage = objectMapper.readValue(record.value(), DlqMessage.class);
            processDlqMessage(dlqMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void processDlqMessage(DlqMessage dlqMessage) {
        log.warn("🔥 DLQ Message processed: originalTopic={}, errorType={}, retryCount={}",
                dlqMessage.getOriginalTopic(), dlqMessage.getErrorType(), dlqMessage.getRetryCount());

        // Could trigger alerts, notifications, etc.
        // Example: Send to monitoring system, create ticket, etc.
    }

}
