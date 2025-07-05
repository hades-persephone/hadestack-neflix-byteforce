package io.watch.auth.repository;

import io.watch.auth.entity.Department;
import io.watch.auth.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Position entities.
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    
    /**
     * Find a position by title.
     *
     * @param title the title to search for
     * @return an Optional containing the position if found, or empty if not found
     */
    Optional<Position> findByTitle(String title);
    
    /**
     * Find all positions in a department.
     *
     * @param department the department to search for
     * @return a list of positions in the department
     */
    List<Position> findByDepartment(Department department);
    
    /**
     * Find a position by title and department.
     *
     * @param title the title to search for
     * @param department the department to search for
     * @return an Optional containing the position if found, or empty if not found
     */
    Optional<Position> findByTitleAndDepartment(String title, Department department);
}