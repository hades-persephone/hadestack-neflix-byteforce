package io.watch.history.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .recordExceptions(IOException.class, TimeoutException.class)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public CircuitBreaker myServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ActionHistoryService");
    }

    @Bean
    public Retry cassandraRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(RuntimeException.class)
                .build();
        return Retry.of("cassandraRetry", config);
    }

    @Bean
    public Bulkhead cassandraBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();

        return Bulkhead.of("cassandraBulkhead", config);
    }

    @Bean
    public TimeLimiter timeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiter.of(config);
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return Executors.newFixedThreadPool(10);
    }

}
