package io.watch.auth.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.watch.auth.dto.PermissionMapResponse;
import io.watch.auth.entity.PermissionMapping;
import io.watch.auth.repository.PermissionMappingRepository;
import io.watch.auth.service.PermissionMappingService;
import io.watch.auth.util.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the PermissionMappingService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionMappingServiceImpl implements PermissionMappingService {

    private final PermissionMappingRepository permissionMappingRepository;
    private final CacheUtils cacheUtils;

    /**
     * Get a map of HTTP methods + paths to permissions.
     * This method is cached to improve performance.
     *
     * @return a PermissionMapResponse containing the mappings
     */
    @Override
    @Cacheable(value = "permissionMap", key = "'all'")
    public Mono<PermissionMapResponse> getPermissionMap() {
        return Mono.fromCallable(() -> {
            List<PermissionMapping> mappings = permissionMappingRepository.findAll();
            return buildPermissionMapResponse(mappings);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get a map of HTTP methods + paths to permissions for a specific tenant.
     * This method is cached to improve performance.
     *
     * @param tenantId the tenant ID
     * @return a PermissionMapResponse containing the mappings
     */
    @Override
    @Cacheable(value = "permissionMap", key = "#tenantId")
    public Mono<PermissionMapResponse> getPermissionMapForTenant(String tenantId) {
        return Mono.fromCallable(() -> {
            List<PermissionMapping> mappings = permissionMappingRepository.findByTenantId(tenantId);
            return buildPermissionMapResponse(mappings);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get a map of HTTP methods + paths to permissions with contextual filters.
     * This method is cached to improve performance.
     *
     * @param tenantId     the tenant ID (optional)
     * @param departmentId the department ID (optional)
     * @param projectId    the project ID (optional)
     * @return a PermissionMapResponse containing the mappings
     */
    @Override
    @Cacheable(value = "permissionMap", key = "#tenantId + '-' + #departmentId + '-' + #projectId")
    public Mono<PermissionMapResponse> getPermissionMapWithContextualFilters(
            String tenantId, String departmentId, String projectId) {
        return Mono.fromCallable(() -> {
            List<PermissionMapping> mappings = permissionMappingRepository.findWithContextualFilters(
                    tenantId, departmentId, projectId);
            return buildPermissionMapResponse(mappings);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Check if an endpoint is public (can be accessed without authentication).
     *
     * @param httpMethod the HTTP method
     * @param path       the path
     * @return true if the endpoint is public, false otherwise
     */
    @Override
    public boolean isPublicEndpoint(String httpMethod, String path) {
        String key = httpMethod + ":" + path;
        return cacheUtils.getFromCache("publicEndpoints", key, Boolean.class)
                .orElseGet(() -> {
                    boolean isPublic = permissionMappingRepository.findByHttpMethodAndPathPattern(httpMethod, path)
                            .map(PermissionMapping::isPublic)
                            .orElse(false);
                    cacheUtils.putInCache("publicEndpoints", key, isPublic);
                    return isPublic;
                });
    }

    /**
     * Get the permission required for an endpoint.
     *
     * @param httpMethod the HTTP method
     * @param path       the path
     * @return the permission name, or null if no permission is required
     */
    @Override
    public String getPermissionForEndpoint(String httpMethod, String path) {
        String key = httpMethod + ":" + path;
        return cacheUtils.getFromCache("endpointPermissions", key, String.class)
                .orElseGet(() -> {
                    String permission = permissionMappingRepository.findByHttpMethodAndPathPattern(httpMethod, path)
                            .map(mapping -> mapping.getPermission().getName())
                            .orElse(null);
                    if (permission != null) {
                        cacheUtils.putInCache("endpointPermissions", key, permission);
                    }
                    return permission;
                });
    }

    /**
     * Get the contextual requirements for an endpoint.
     *
     * @param httpMethod the HTTP method
     * @param path       the path
     * @return a map of contextual attribute names to requirements, or an empty map if no contextual requirements
     */
    @Override
    public Mono<Map<String, String>> getContextualRequirementsForEndpoint(String httpMethod, String path) {
        String key = httpMethod + ":" + path;
        return Mono.fromCallable(() -> {
            return cacheUtils.getFromCache("contextualRequirements", key, new TypeReference<Map<String, String>>() {})
                    .orElseGet(() -> {
                        Map<String, String> requirements = new HashMap<>();
                        permissionMappingRepository.findByHttpMethodAndPathPattern(httpMethod, path)
                                .ifPresent(mapping -> {
                                    if (mapping.getTenantId() != null) {
                                        requirements.put("tenantId", "required");
                                    }
                                    if (mapping.getDepartmentId() != null) {
                                        requirements.put("departmentId", "required");
                                    }
                                    if (mapping.getProjectId() != null) {
                                        requirements.put("projectId", "required");
                                    }
                                });
                        cacheUtils.putInCache("contextualRequirements", key, requirements);
                        return requirements;
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Build a PermissionMapResponse from a list of PermissionMapping entities.
     *
     * @param mappings the list of PermissionMapping entities
     * @return a PermissionMapResponse containing the mappings
     */
    private PermissionMapResponse buildPermissionMapResponse(List<PermissionMapping> mappings) {
        Map<String, String> permissionMap = new HashMap<>();
        List<String> publicEndpoints = mappings.stream()
                .filter(PermissionMapping::isPublic)
                .map(mapping -> mapping.getHttpMethod() + ":" + mapping.getPathPattern())
                .collect(Collectors.toList());

        Map<String, Map<String, String>> contextualRequirements = new HashMap<>();

        for (PermissionMapping mapping : mappings) {
            String key = mapping.getHttpMethod() + ":" + mapping.getPathPattern();
            permissionMap.put(key, mapping.getPermission().getName());

            Map<String, String> requirements = new HashMap<>();
            if (mapping.getTenantId() != null) {
                requirements.put("tenantId", "required");
            }
            if (mapping.getDepartmentId() != null) {
                requirements.put("departmentId", "required");
            }
            if (mapping.getProjectId() != null) {
                requirements.put("projectId", "required");
            }

            if (!requirements.isEmpty()) {
                contextualRequirements.put(key, requirements);
            }
        }

        return PermissionMapResponse.builder()
                .permissionMap(permissionMap)
                .publicEndpoints(publicEndpoints)
                .contextualRequirements(contextualRequirements)
                .build();
    }
}