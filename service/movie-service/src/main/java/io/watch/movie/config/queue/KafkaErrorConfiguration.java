package io.watch.movie.config.queue;

import io.watch.movie.handler.kafka.KafkaMetricsService;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaErrorConfiguration {

    @Bean
    public KafkaMetricsService kafkaMetricsService() {
        return new KafkaMetricsService();
    }

}
