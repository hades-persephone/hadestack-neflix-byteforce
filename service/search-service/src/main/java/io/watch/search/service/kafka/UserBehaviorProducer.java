package io.watch.search.service.kafka;

import io.watch.search.model.kafka.UserBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserBehaviorProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserBehaviorProducer.class);
    private static final String DLQ_TOPIC = "search-dlq";

    private final KafkaTemplate<String, UserBehavior> kafkaTemplate;

    public UserBehaviorProducer(KafkaTemplate<String, UserBehavior> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToDlq(UserBehavior behavior) {
        kafkaTemplate.send(DLQ_TOPIC, String.valueOf(behavior.getUserId()), behavior);
        logger.info("Sent user behavior to DLQ: userId={}, profileId={}", behavior.getUserId(), behavior.getProfileId());
    }
}