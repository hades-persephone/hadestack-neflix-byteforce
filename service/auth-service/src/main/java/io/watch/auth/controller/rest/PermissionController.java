package io.watch.auth.controller.rest;

import io.watch.auth.dto.PermissionMapResponse;
import io.watch.auth.service.PermissionMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller for permission-related endpoints.
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionMappingService permissionMappingService;

    /**
     * Get a map of HTTP methods + paths to permissions.
     * This endpoint is used by OPA to dynamically fetch permissions.
     *
     * @return a PermissionMapResponse containing the mappings
     */
    @GetMapping("/map")
    public Mono<ResponseEntity<PermissionMapResponse>> getPermissionMap() {
        log.info("Fetching permission map");
        return permissionMappingService.getPermissionMap()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get a map of HTTP methods + paths to permissions for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return a PermissionMapResponse containing the mappings
     */
    @GetMapping("/map/tenant")
    public Mono<ResponseEntity<PermissionMapResponse>> getPermissionMapForTenant(
            @RequestParam String tenantId) {
        log.info("Fetching permission map for tenant: {}", tenantId);
        return permissionMappingService.getPermissionMapForTenant(tenantId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get a map of HTTP methods + paths to permissions with contextual filters.
     *
     * @param tenantId     the tenant ID (optional)
     * @param departmentId the department ID (optional)
     * @param projectId    the project ID (optional)
     * @return a PermissionMapResponse containing the mappings
     */
    @GetMapping("/map/context")
    public Mono<ResponseEntity<PermissionMapResponse>> getPermissionMapWithContextualFilters(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String projectId) {
        log.info("Fetching permission map with contextual filters: tenantId={}, departmentId={}, projectId={}",
                tenantId, departmentId, projectId);
        return permissionMappingService.getPermissionMapWithContextualFilters(tenantId, departmentId, projectId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}