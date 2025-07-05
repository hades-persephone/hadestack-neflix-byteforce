package io.watch.auth.controller;

import io.watch.auth.entity.Department;
import io.watch.auth.entity.Organization;
import io.watch.auth.repository.DepartmentRepository;
import io.watch.auth.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing departments.
 */
@RestController
@RequestMapping("/api/auth/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Get all departments.
     *
     * @return a list of all departments
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    /**
     * Get a department by ID.
     *
     * @param id the ID of the department
     * @return the department if found, or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<Department> getDepartmentById(@PathVariable UUID id) {
        Optional<Department> department = departmentRepository.findById(id);
        return department.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all departments in an organization.
     *
     * @param organizationId the ID of the organization
     * @return a list of departments in the organization
     */
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<List<Department>> getDepartmentsByOrganization(@PathVariable UUID organizationId) {
        Optional<Organization> organization = organizationRepository.findById(organizationId);
        if (organization.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(departmentRepository.findByOrganization(organization.get()));
    }

    /**
     * Create a new department.
     *
     * @param department the department to create
     * @return the created department
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    /**
     * Update an existing department.
     *
     * @param id the ID of the department to update
     * @param department the updated department data
     * @return the updated department if found, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<Department> updateDepartment(@PathVariable UUID id, @RequestBody Department department) {
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        department.setId(id);
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    /**
     * Delete a department.
     *
     * @param id the ID of the department to delete
     * @return 204 No Content if successful, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        departmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}