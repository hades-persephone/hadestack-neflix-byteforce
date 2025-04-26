package io.watch.auth.config.webclient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
public class WebClientConfig {
    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Bean(name = "userServiceWebClient")
    public WebClient userServiceWebClient(CircuitBreakerRegistry circuitBreakerRegistry) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");

        return WebClient.builder()
                .filter((request, next) -> {
                    Supplier<Mono<ClientResponse>> decoratedSupplier =
                            CircuitBreaker.decorateSupplier(circuitBreaker, () -> next.exchange(request));
                    return Mono.defer(decoratedSupplier);
                })
                .baseUrl(userServiceUrl)
                .build();
    }
}
