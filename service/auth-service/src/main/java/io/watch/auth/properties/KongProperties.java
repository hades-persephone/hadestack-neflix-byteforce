package io.watch.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Kong API Gateway.
 */
@Data
@Component
@ConfigurationProperties(prefix = "kong")
public class KongProperties {
    private String adminUrl;
    private String proxyUrl;
    private String apiKey;
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;
    private boolean enableRouteCache = true;
    private int routeCacheTtl = 300; // 5 minutes
}
