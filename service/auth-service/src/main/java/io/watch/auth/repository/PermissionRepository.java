package io.watch.auth.repository;

import io.watch.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Permission entities.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    /**
     * Find a permission by name.
     *
     * @param name the name of the permission to search for
     * @return an Optional containing the permission if found, or empty if not found
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Check if a permission exists with the given name.
     *
     * @param name the name to check
     * @return true if a permission exists with the name, false otherwise
     */
    boolean existsByName(String name);
}