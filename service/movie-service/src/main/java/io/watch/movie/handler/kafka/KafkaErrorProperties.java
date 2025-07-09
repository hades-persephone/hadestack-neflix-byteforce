package io.watch.movie.handler.kafka;

import io.github.resilience4j.retry.RetryConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "kafka.error")
public class KafkaErrorProperties {

    private int maxRetryAttempts = 3;
    private long baseRetryDelayMs = 1000;
    private long maxRetryDelayMs = 30000;
    private boolean enableDlq = true;
    private String dlqSuffix = "-dlq";
    private boolean enableCompression = true;
    private int maxMessageSizeBytes = 1024 * 1024;
    private boolean storeFailedMessages = true;
    private int failedMessageRetentionDays = 30;

    private RetryConfig retry = new RetryConfig();

    @Data
    public static class RetryConfig {
        private boolean exponentialBackoff = true;
        private double backoffMultiplier = 2.0;
        private List<String> retryableErrors = Arrays.asList(
                "NetworkException", "TimeoutException", "DisconnectException",
                "NotLeaderForPartitionException", "LeaderNotAvailableException");
    }

}
