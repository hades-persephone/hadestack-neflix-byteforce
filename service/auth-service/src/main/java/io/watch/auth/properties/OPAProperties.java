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

    /**
     * The base URL of the OPA server.
     */
    private String url = "http://localhost:8181";

    /**
     * The policy path to use for authorization decisions.
     */
    private String policyPath = "/v1/data/authz/allow";

    /**
     * The timeout in milliseconds for OPA requests.
     */
    private int timeoutMs = 5000;

    /**
     * Whether to enable caching of OPA decisions.
     */
    private boolean enableCaching = true;

    /**
     * The TTL (time to live) in seconds for cached OPA decisions.
     */
    private int cacheTtlSeconds = 300;

    /**
     * The maximum size of the OPA decision cache.
     */
    private int cacheMaxSize = 10000;

    /**
     * Whether to enable audit logging for OPA decisions.
     */
    private boolean enableAuditLogging = true;

    /**
     * Whether to enable fallback mode when OPA is unavailable.
     */
    private boolean enableFallback = true;

    /**
     * The default decision to use when OPA is unavailable and fallback is enabled.
     * true = allow, false = deny
     */
    private boolean fallbackDecision = false;
}
