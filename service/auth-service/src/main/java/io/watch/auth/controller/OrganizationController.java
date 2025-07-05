package io.watch.auth.controller;

import io.watch.auth.entity.Organization;
import io.watch.auth.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing organizations.
 */
@RestController
@RequestMapping("/api/auth/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    /**
     * Get all organizations.
     *
     * @return a list of all organizations
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationRepository.findAll());
    }

    /**
     * Get an organization by ID.
     *
     * @param id the ID of the organization
     * @return the organization if found, or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable UUID id) {
        Optional<Organization> organization = organizationRepository.findById(id);
        return organization.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new organization.
     *
     * @param organization the organization to create
     * @return the created organization
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        return ResponseEntity.ok(organizationRepository.save(organization));
    }

    /**
     * Update an existing organization.
     *
     * @param id the ID of the organization to update
     * @param organization the updated organization data
     * @return the updated organization if found, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID id, @RequestBody Organization organization) {
        if (!organizationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        organization.setId(id);
        return ResponseEntity.ok(organizationRepository.save(organization));
    }

    /**
     * Delete an organization.
     *
     * @param id the ID of the organization to delete
     * @return 204 No Content if successful, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        if (!organizationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        organizationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}