package io.watch.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.watch.auth.properties.OPAProperties;
import io.watch.auth.util.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with OPA (Open Policy Agent).
 * Used for making authorization decisions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OPAClient {

    private final OPAProperties opaProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CacheUtils cacheUtils;

    /**
     * Check if a request is authorized.
     *
     * @param input the input to send to OPA
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorized(Map<String, Object> input) {
        String cacheKey = generateCacheKey(input);

        // Check cache first if caching is enabled
        if (opaProperties.isEnableCaching()) {
            Optional<Boolean> cachedDecision = cacheUtils.getFromCache("opaDecisions", cacheKey, Boolean.class);
            if (cachedDecision.isPresent()) {
                log.debug("Using cached OPA decision for key: {}", cacheKey);
                return cachedDecision.get();
            }
        }

        try {
            // Prepare the request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("input", objectMapper.valueToTree(input));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make the request to OPA
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    opaProperties.getUrl() + opaProperties.getPolicyPath(),
                    entity,
                    JsonNode.class
            );

            // Parse the response
            boolean decision = false;
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode resultNode = response.getBody().path("result");
                if (!resultNode.isMissingNode() && resultNode.isBoolean()) {
                    decision = resultNode.asBoolean();
                }
            }

            // Audit log the decision if enabled
            if (opaProperties.isEnableAuditLogging()) {
                log.info("OPA Authorization Decision: {} for input: {}", decision, input);
            }

            // Cache the decision if caching is enabled
            if (opaProperties.isEnableCaching()) {
                cacheUtils.putInCache("opaDecisions", cacheKey, decision);
            }

            return decision;
        } catch (RestClientException e) {
            log.error("Error calling OPA: {}", e.getMessage(), e);

            // Use fallback decision if enabled
            if (opaProperties.isEnableFallback()) {
                log.warn("Using fallback decision: {}", opaProperties.isFallbackDecision());
                return opaProperties.isFallbackDecision();
            }

            // Default to deny if fallback is not enabled
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during OPA authorization: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate a cache key for the input.
     *
     * @param input the input to generate a key for
     * @return the cache key
     */
    private String generateCacheKey(Map<String, Object> input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (Exception e) {
            log.error("Error generating cache key: {}", e.getMessage(), e);
            return input.toString();
        }
    }
}
