package io.watch.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.watch.auth.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client for interacting with Keycloak.
 * Used for authentication and user management.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClient {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Authenticate a user with username and password.
     *
     * @param username the username
     * @param password the password
     * @return an Optional containing the access token if authentication is successful, empty otherwise
     */
    public Optional<String> authenticate(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", keycloakProperties.getClientId());
            map.add("client_secret", keycloakProperties.getClientSecret());
            map.add("grant_type", "password");
            map.add("username", username);
            map.add("password", password);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    keycloakProperties.getAuthServerUrl() + "/realms/" + keycloakProperties.getRealm() + "/protocol/openid-connect/token",
                    request,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.ofNullable(response.getBody().path("access_token").asText());
            }

            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Error authenticating with Keycloak: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get user information from Keycloak.
     *
     * @param token the access token
     * @return a Map containing user information if successful, empty otherwise
     */
    public Optional<Map<String, Object>> getUserInfo(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    keycloakProperties.getAuthServerUrl() + "/realms/" + keycloakProperties.getRealm() + "/protocol/openid-connect/userinfo",
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userInfo = objectMapper.convertValue(response.getBody(), Map.class);
                return Optional.of(userInfo);
            }

            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Error getting user info from Keycloak: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Validate a token with Keycloak.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", keycloakProperties.getClientId());
            map.add("client_secret", keycloakProperties.getClientSecret());
            map.add("token", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    keycloakProperties.getAuthServerUrl() + "/realms/" + keycloakProperties.getRealm() + "/protocol/openid-connect/token/introspect",
                    request,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().path("active").asBoolean(false);
            }

            return false;
        } catch (RestClientException e) {
            log.error("Error validating token with Keycloak: {}", e.getMessage(), e);
            return false;
        }
    }
}
