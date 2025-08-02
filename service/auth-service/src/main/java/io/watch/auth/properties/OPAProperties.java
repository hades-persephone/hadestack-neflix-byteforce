package io.watch.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties for OPA (Open Policy Agent) configuration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.opa")
public class OPAProperties {
    private String url = "http://localhost:8181";
    private String policyPath = "/v1/data/authz/allow";
    private int timeoutMs = 5000;
    private boolean enableCaching = true;
    private int cacheTtlSeconds = 300;
    private int cacheMaxSize = 10000;
    private boolean enableAuditLogging = true;
    private boolean enableFallback = true;
    private boolean fallbackDecision = false;
}
