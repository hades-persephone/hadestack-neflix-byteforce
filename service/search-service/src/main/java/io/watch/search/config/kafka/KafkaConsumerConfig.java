package io.watch.search.config.kafka;

import io.watch.search.model.entity.Movie;
import io.watch.search.model.kafka.UserBehavior;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String trustedPackages;

    private static final String SEARCH_MOVIES = "search-movies";
    private static final String SEARCH_BEHAVIORS = "search-behaviors";
    private static final String DLQ_TOPIC = "search-dlq";

    @Bean
    public ConsumerFactory<String, Movie> movieConsumerFactory() {
        return getConsumerFactory(SEARCH_MOVIES, Movie.class);
    }

    @Bean
    public ConsumerFactory<String, UserBehavior> behaviorConsumerFactory() {
        return getConsumerFactory(SEARCH_BEHAVIORS, UserBehavior.class);
    }

    @Bean(name = "movieKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Movie> movieKafkaListenerContainerFactory(
            KafkaTemplate<String, Movie> movieKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, Movie> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(movieConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(movieKafkaTemplate));
        return factory;
    }

    @Bean(name = "behaviorKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserBehavior> behaviorKafkaListenerContainerFactory(
            KafkaTemplate<String, UserBehavior> behaviorKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, UserBehavior> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(behaviorConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(behaviorKafkaTemplate));
        return factory;
    }

    private <T> ConsumerFactory<String, T> getConsumerFactory(String groupId, Class<T> valueType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);

        // Note: Additional configurations are set in application.properties
        // These include:
        // - auto-offset-reset
        // - max-poll-records
        // - enable-auto-commit
        // - session-timeout
        // - heartbeat-interval
        // - max-poll-interval

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(valueType, false)
        );
    }

    private <T> DefaultErrorHandler errorHandler(KafkaTemplate<String, T> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(DLQ_TOPIC, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer);
        errorHandler.addRetryableExceptions(RuntimeException.class);
        return errorHandler;
    }

}
