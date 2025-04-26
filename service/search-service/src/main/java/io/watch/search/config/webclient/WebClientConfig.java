package io.watch.search.config.webclient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Configuration
public class WebClientConfig {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

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

    @Bean(name = "authServiceWebClient")
    public WebClient authServiceWebClient(CircuitBreakerRegistry circuitBreakerRegistry) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("authService");

        return WebClient.builder()
                .filter((request, next) -> {
                    Supplier<Mono<ClientResponse>> decoratedSupplier =
                            CircuitBreaker.decorateSupplier(circuitBreaker, () -> next.exchange(request));
                    return Mono.defer(decoratedSupplier);
                })
                .baseUrl(authServiceUrl)
                .build();
    }
}
