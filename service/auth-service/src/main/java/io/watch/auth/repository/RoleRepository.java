package io.watch.auth.repository;

import io.watch.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Role entities.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    /**
     * Find a role by name.
     *
     * @param name the name of the role to search for
     * @return an Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if a role exists with the given name.
     *
     * @param name the name to check
     * @return true if a role exists with the name, false otherwise
     */
    boolean existsByName(String name);

    List<Role> findByNameIn(List<String> roleNames);
}