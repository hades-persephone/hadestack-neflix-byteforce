package io.watch.auth.controller;

import io.watch.auth.entity.Department;
import io.watch.auth.entity.Position;
import io.watch.auth.repository.DepartmentRepository;
import io.watch.auth.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing positions.
 */
@RestController
@RequestMapping("/api/auth/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get all positions.
     *
     * @return a list of all positions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN', 'POSITION_ADMIN')")
    public ResponseEntity<List<Position>> getAllPositions() {
        return ResponseEntity.ok(positionRepository.findAll());
    }

    /**
     * Get a position by ID.
     *
     * @param id the ID of the position
     * @return the position if found, or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN', 'POSITION_ADMIN')")
    public ResponseEntity<Position> getPositionById(@PathVariable UUID id) {
        Optional<Position> position = positionRepository.findById(id);
        return position.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all positions in a department.
     *
     * @param departmentId the ID of the department
     * @return a list of positions in the department
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN', 'POSITION_ADMIN')")
    public ResponseEntity<List<Position>> getPositionsByDepartment(@PathVariable UUID departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if (department.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(positionRepository.findByDepartment(department.get()));
    }

    /**
     * Create a new position.
     *
     * @param position the position to create
     * @return the created position
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        return ResponseEntity.ok(positionRepository.save(position));
    }

    /**
     * Update an existing position.
     *
     * @param id the ID of the position to update
     * @param position the updated position data
     * @return the updated position if found, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<Position> updatePosition(@PathVariable UUID id, @RequestBody Position position) {
        if (!positionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        position.setId(id);
        return ResponseEntity.ok(positionRepository.save(position));
    }

    /**
     * Delete a position.
     *
     * @param id the ID of the position to delete
     * @return 204 No Content if successful, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<Void> deletePosition(@PathVariable UUID id) {
        if (!positionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        positionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}