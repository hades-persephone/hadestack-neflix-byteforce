package io.watch.history.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.watch.history.entity.ActionHistoryByAction;
import io.watch.history.entity.ActionHistoryByUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
@Slf4j
@RequiredArgsConstructor
public class CassandraCircuitBreakerService {

    private final CircuitBreaker circuitBreaker;
    private final ActionHistoryService actionHistoryService;

    public void saveWithCircuitBreaker(ActionHistoryByAction activity) {
        Supplier<CompletableFuture<Void>> decoratedSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker,
                        () -> actionHistoryService.saveUserActivityWithRetry(activity));

        try {
            decoratedSupplier.get();
        } catch (CallNotPermittedException e) {
            log.warn("Circuit breaker is open, storing activity in fallback storage");
            // Fallback to alternative storage
            storeInFallbackStorage(activity);
        }
    }

    private void storeInFallbackStorage(ActionHistoryByAction activity) {
        // Store in Redis, file, or queue for later processing
        log.info("Storing activity in fallback storage: {}", activity.getId());
    }
}