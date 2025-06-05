package io.watch.history.config;

import io.github.resilience4j.circuitbreaker.*;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.time.Duration;
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
}
