package io.watch.auth.config;

import io.watch.auth.properties.OPAProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration class for RestTemplate.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Create a RestTemplate bean with configured timeout.
     *
     * @param opaProperties the OPA properties
     * @return the RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(OPAProperties opaProperties) {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(opaProperties.getTimeoutMs()))
                .readTimeout(Duration.ofMillis(opaProperties.getTimeoutMs()))
                .build();
    }

    /**
     * Create an ObjectMapper bean.
     *
     * @return the ObjectMapper
     */
    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}
