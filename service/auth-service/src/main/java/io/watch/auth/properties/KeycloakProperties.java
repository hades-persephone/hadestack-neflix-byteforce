package io.watch.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Keycloak.
 */
@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String authServerUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;
}
