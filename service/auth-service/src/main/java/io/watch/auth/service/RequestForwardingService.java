package io.watch.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Service interface for request forwarding operations.
 */
public interface RequestForwardingService {
    
    /**
     * Forward a request to a service.
     *
     * @param serviceName the name of the service
     * @param path the path to forward to
     * @param method the HTTP method
     * @param headers the HTTP headers
     * @param body the request body (optional)
     * @return the response from the service
     */
    ResponseEntity<String> forwardRequest(String serviceName, String path, HttpMethod method, 
                                         HttpHeaders headers, Object body);
    
    /**
     * Check if a service exists.
     *
     * @param serviceName the name of the service
     * @return true if the service exists, false otherwise
     */
    boolean serviceExists(String serviceName);
    
    /**
     * Get the URL for a service.
     *
     * @param serviceName the name of the service
     * @return the URL of the service, or null if not found
     */
    String getServiceUrl(String serviceName);
}