package io.watch.auth.service;

import io.watch.auth.dto.PermissionMapResponse;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing permission mappings.
 */
public interface PermissionMappingService {
    
    /**
     * Get a map of HTTP methods + paths to permissions.
     * 
     * @return a PermissionMapResponse containing the mappings
     */
    Mono<PermissionMapResponse> getPermissionMap();
    
    /**
     * Get a map of HTTP methods + paths to permissions for a specific tenant.
     * 
     * @param tenantId the tenant ID
     * @return a PermissionMapResponse containing the mappings
     */
    Mono<PermissionMapResponse> getPermissionMapForTenant(String tenantId);
    
    /**
     * Get a map of HTTP methods + paths to permissions with contextual filters.
     * 
     * @param tenantId the tenant ID (optional)
     * @param departmentId the department ID (optional)
     * @param projectId the project ID (optional)
     * @return a PermissionMapResponse containing the mappings
     */
    Mono<PermissionMapResponse> getPermissionMapWithContextualFilters(
            String tenantId, String departmentId, String projectId);
    
    /**
     * Check if an endpoint is public (can be accessed without authentication).
     * 
     * @param httpMethod the HTTP method
     * @param path the path
     * @return true if the endpoint is public, false otherwise
     */
    boolean isPublicEndpoint(String httpMethod, String path);
    
    /**
     * Get the permission required for an endpoint.
     * 
     * @param httpMethod the HTTP method
     * @param path the path
     * @return the permission name, or null if no permission is required
     */
    String getPermissionForEndpoint(String httpMethod, String path);
    
    /**
     * Get the contextual requirements for an endpoint.
     * 
     * @param httpMethod the HTTP method
     * @param path the path
     * @return a map of contextual attribute names to requirements, or an empty map if no contextual requirements
     */
    Mono<java.util.Map<String, String>> getContextualRequirementsForEndpoint(String httpMethod, String path);
}