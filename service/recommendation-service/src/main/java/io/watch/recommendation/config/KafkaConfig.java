package io.watch.recommendation.config;

import io.watch.recommendation.model.UserBehavior;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
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
public class KafkaConfig {

    @Value("spring.kafka.bootstrap-servers")
    private String bootstrapServers;

    @Value("spring.kafka.consumer.properties.spring.json.trusted.packages")
    private String trustedPackages;

    private static final String RECOMMENDATION_REDIS = "recommendation-redis";
    private static final String RECOMMENDATION_CASSANDRA = "recommendation-cassandra";
    private static final String RECOMMENDATION_RETRY = "recommendation-retry";
    private static final String DLQ_TOPIC = "user-behaviors-dlq";

    @Bean
    public ConsumerFactory<String, Object> redisConsumerFactory() {
        return getStringObjectConsumerFactory(RECOMMENDATION_REDIS);
    }

    @Bean
    public ConsumerFactory<String, Object> cassandraConsumerFactory() {
        return getStringObjectConsumerFactory(RECOMMENDATION_CASSANDRA);
    }

    @Bean
    public ConsumerFactory<String, Object> retryConsumerFactory() {
        return getStringObjectConsumerFactory(RECOMMENDATION_RETRY);
    }

    @Bean(name = "redisKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> redisKafkaListenerContainerFactory(
            KafkaTemplate<String, UserBehavior> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(redisConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }


    @Bean(name = "cassandraKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> cassandraKafkaListenerContainerFactory(
            KafkaTemplate<String, UserBehavior> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cassandraConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }

    @Bean(name = "retryKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> retryKafkaListenerContainerFactory(
            KafkaTemplate<String, UserBehavior> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(retryConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }


    @NotNull
    private ConsumerFactory<String, Object> getStringObjectConsumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(Object.class, false)
        );
    }

    private DefaultErrorHandler errorHandler(KafkaTemplate<String, UserBehavior> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(DLQ_TOPIC, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer);
        errorHandler.addRetryableExceptions(RuntimeException.class); // Example
        return errorHandler;
    }
}

