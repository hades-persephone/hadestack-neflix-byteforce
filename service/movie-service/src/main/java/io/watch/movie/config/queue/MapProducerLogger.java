package io.watch.movie.config.queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MapProducerLogger implements ProducerListener<String, Map<String, Object>> {
    @Override
    public void onSuccess(ProducerRecord<String, Map<String, Object>> producerRecord, RecordMetadata recordMetadata) {
        log.debug("✅ Sent to topic {} partition {} offset {}",
                recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
        ProducerListener.super.onSuccess(producerRecord, recordMetadata);
    }

    @Override
    public void onError(ProducerRecord<String, Map<String, Object>> producerRecord, RecordMetadata recordMetadata, Exception exception) {
        log.debug("❌ Error sending Kafka message: {}", exception.getMessage());
        ProducerListener.super.onError(producerRecord, recordMetadata, exception);
    }
}
