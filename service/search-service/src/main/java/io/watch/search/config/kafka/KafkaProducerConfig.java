package io.watch.search.config.kafka;

import io.watch.search.model.entity.Movie;
import io.watch.search.model.kafka.UserBehavior;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Movie> movieProducerFactory() {
        return createProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, Movie> movieKafkaTemplate() {
        return new KafkaTemplate<>(movieProducerFactory());
    }

    @Bean
    public ProducerFactory<String, UserBehavior> behaviorProducerFactory() {
        return createProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, UserBehavior> behaviorKafkaTemplate() {
        return new KafkaTemplate<>(behaviorProducerFactory());
    }

    /**
     * Creates a producer factory with configurations to prevent deadlocks and broker failures
     */
    private <T> ProducerFactory<String, T> createProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Note: Additional configurations are set in application.properties
        // These include:
        // - acks
        // - retries
        // - retry.backoff.ms
        // - max.block.ms
        // - request.timeout.ms
        // - delivery.timeout.ms
        // - buffer.memory
        // - batch.size
        // - linger.ms
        // - enable.idempotence

        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
