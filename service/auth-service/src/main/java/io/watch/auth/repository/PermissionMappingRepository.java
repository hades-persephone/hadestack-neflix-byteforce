package io.watch.auth.repository;

import io.watch.auth.entity.PermissionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PermissionMapping entity.
 */
@Repository
public interface PermissionMappingRepository extends JpaRepository<PermissionMapping, Long> {

    /**
     * Find a permission mapping by HTTP method and path pattern.
     *
     * @param httpMethod the HTTP method
     * @param pathPattern the path pattern
     * @return an Optional containing the permission mapping if found
     */
    Optional<PermissionMapping> findByHttpMethodAndPathPattern(String httpMethod, String pathPattern);

    /**
     * Find all permission mappings for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return a list of permission mappings
     */
    List<PermissionMapping> findByTenantId(String tenantId);

    /**
     * Find all permission mappings that are public (can be accessed without authentication).
     *
     * @return a list of public permission mappings
     */
    List<PermissionMapping> findByIsPublicTrue();

    /**
     * Find all permission mappings with contextual filters.
     *
     * @param tenantId the tenant ID (optional)
     * @param departmentId the department ID (optional)
     * @param projectId the project ID (optional)
     * @return a list of permission mappings matching the filters
     */
    @Query("SELECT pm FROM PermissionMapping pm WHERE " +
           "(:tenantId IS NULL OR pm.tenantId = :tenantId) AND " +
           "(:departmentId IS NULL OR pm.departmentId = :departmentId) AND " +
           "(:projectId IS NULL OR pm.projectId = :projectId)")
    List<PermissionMapping> findWithContextualFilters(String tenantId, String departmentId, String projectId);
}