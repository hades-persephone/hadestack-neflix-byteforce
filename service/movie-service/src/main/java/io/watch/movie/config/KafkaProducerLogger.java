package io.watch.movie.config;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerLogger implements ProducerListener<String, String> {

    @Override
    public void onSuccess(ProducerRecord<String, String> record, RecordMetadata metadata) {
        System.out.printf("✅ Sent to topic %s partition %d offset %d%n",
                metadata.topic(), metadata.partition(), metadata.offset());
    }

    @Override
    public void onError(ProducerRecord<String, String> record, RecordMetadata metadata, Exception exception) {
        System.err.println("❌ Error sending Kafka message: " + exception.getMessage());
    }
}
