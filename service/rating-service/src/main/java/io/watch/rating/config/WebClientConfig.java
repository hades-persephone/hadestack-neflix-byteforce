package io.watch.rating.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("movieWebClient")
    public WebClient movieWebClient(WebClient.Builder webClientBuilder, @Value("${movie.service.url}") String baseUrl) {
        return webClientBuilder.baseUrl(baseUrl).build();
    }

    @Bean
    @Qualifier("userWebClient")
    public WebClient userWebClient(WebClient.Builder webClientBuilder, @Value("${user.service.url}") String baseUrl) {
        return webClientBuilder.baseUrl(baseUrl).build();
    }
}