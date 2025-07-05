package io.watch.auth.repository;

import io.watch.auth.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Organization entities.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    /**
     * Find an organization by name.
     *
     * @param name the name to search for
     * @return an Optional containing the organization if found, or empty if not found
     */
    Optional<Organization> findByName(String name);
    
    /**
     * Check if an organization exists with the given name.
     *
     * @param name the name to check
     * @return true if an organization exists with the name, false otherwise
     */
    boolean existsByName(String name);
}