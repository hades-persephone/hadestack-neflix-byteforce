package io.watch.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.watch.auth.properties.KongProperties;
import io.watch.auth.util.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client for interacting with Kong API Gateway.
 * Used for request forwarding and API management.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KongClient {

    private final KongProperties kongProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CacheUtils cacheUtils;

    /**
     * Forward a request to a service through Kong.
     *
     * @param serviceName the name of the service
     * @param path the path to forward to
     * @param method the HTTP method
     * @param headers the HTTP headers
     * @param body the request body (optional)
     * @return the response from the service
     */
    public ResponseEntity<String> forwardRequest(String serviceName, String path, HttpMethod method, 
                                                HttpHeaders headers, Object body) {
        try {
            // Get the service URL from Kong
            String serviceUrl = getServiceUrl(serviceName);
            if (serviceUrl == null) {
                log.error("Service not found in Kong: {}", serviceName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
            }

            // Prepare the request
            HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

            // Forward the request through Kong
            return restTemplate.exchange(
                    serviceUrl + path,
                    method,
                    requestEntity,
                    String.class
            );
        } catch (RestClientException e) {
            log.error("Error forwarding request to Kong: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error forwarding request: " + e.getMessage());
        }
    }

    /**
     * Get the URL for a service from Kong.
     *
     * @param serviceName the name of the service
     * @return the URL of the service, or null if not found
     */
    public String getServiceUrl(String serviceName) {
        // Check cache first if caching is enabled
        if (kongProperties.isEnableRouteCache()) {
            String cacheKey = "service_url_" + serviceName;
            Optional<String> cachedUrl = cacheUtils.getFromCache("kongRoutes", cacheKey, String.class);
            if (cachedUrl.isPresent()) {
                log.debug("Using cached service URL for {}: {}", serviceName, cachedUrl.get());
                return cachedUrl.get();
            }
        }

        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (kongProperties.getApiKey() != null && !kongProperties.getApiKey().isEmpty()) {
                headers.set("apikey", kongProperties.getApiKey());
            }

            // Make the request to Kong Admin API
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    kongProperties.getAdminUrl() + "/services/" + serviceName,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            // Parse the response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String protocol = response.getBody().path("protocol").asText("http");
                String host = response.getBody().path("host").asText();
                int port = response.getBody().path("port").asInt(80);
                String path = response.getBody().path("path").asText("");

                String serviceUrl = protocol + "://" + host + ":" + port + path;

                // Cache the result if caching is enabled
                if (kongProperties.isEnableRouteCache()) {
                    String cacheKey = "service_url_" + serviceName;
                    cacheUtils.putInCache("kongRoutes", cacheKey, serviceUrl);
                }

                return serviceUrl;
            }

            log.warn("Service not found in Kong: {}", serviceName);
            return null;
        } catch (RestClientException e) {
            log.error("Error getting service from Kong: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if a service exists in Kong.
     *
     * @param serviceName the name of the service
     * @return true if the service exists, false otherwise
     */
    public boolean serviceExists(String serviceName) {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (kongProperties.getApiKey() != null && !kongProperties.getApiKey().isEmpty()) {
                headers.set("apikey", kongProperties.getApiKey());
            }

            // Make the request to Kong Admin API
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    kongProperties.getAdminUrl() + "/services/" + serviceName,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Error checking if service exists in Kong: {}", e.getMessage(), e);
            return false;
        }
    }
}
