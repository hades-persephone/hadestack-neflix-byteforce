package io.watch.history.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "app.resilience")
public class ResilienceProperties {
    private Duration timeout = Duration.ofSeconds(5);
    private Duration progressUpdateTimeout = Duration.ofSeconds(2);
    private Duration batchTimeout = Duration.ofSeconds(10);
    private Duration healthCheckTimeout = Duration.ofSeconds(3);
    private Duration failedRecordRetention = Duration.ofHours(24);
    private int maxBatchSize = 100;
    private int maxRetryAttempts = 3;
    private Duration retryDelay = Duration.ofSeconds(1);
}
