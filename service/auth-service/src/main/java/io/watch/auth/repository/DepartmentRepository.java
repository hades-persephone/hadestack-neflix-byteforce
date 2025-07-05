package io.watch.auth.repository;

import io.watch.auth.entity.Department;
import io.watch.auth.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Department entities.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    /**
     * Find a department by name.
     *
     * @param name the name to search for
     * @return an Optional containing the department if found, or empty if not found
     */
    Optional<Department> findByName(String name);
    
    /**
     * Check if a department exists with the given name.
     *
     * @param name the name to check
     * @return true if a department exists with the name, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find all departments belonging to an organization.
     *
     * @param organization the organization to search for
     * @return a list of departments belonging to the organization
     */
    List<Department> findByOrganization(Organization organization);
}