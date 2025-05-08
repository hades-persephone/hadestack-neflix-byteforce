package io.watch.user.service;

import io.watch.user.dto.DepartmentDTO;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);
    DepartmentDTO getDepartmentById(UUID id);
    List<DepartmentDTO> getAllDepartments();
    DepartmentDTO updateDepartment(UUID id, DepartmentDTO departmentDTO);
    void deleteDepartment(UUID id);
}
