package io.watch.auth.service;

import java.util.Map;

/**
 * Service interface for authorization operations.
 */
public interface AuthorizationService {
    
    /**
     * Check if a request is authorized.
     *
     * @param input the input data containing request and user information
     * @return true if authorized, false otherwise
     */
    boolean isAuthorized(Map<String, Object> input);
    
    /**
     * Check if an endpoint is public (doesn't require authorization).
     *
     * @param method the HTTP method
     * @param path the request path
     * @return true if the endpoint is public, false otherwise
     */
    boolean isPublicEndpoint(String method, String path);
}