package io.watch.user.service.impl;

import io.watch.user.dto.DepartmentDTO;
import io.watch.user.entity.Department;
import io.watch.user.entity.User;
import io.watch.user.exception.ResourceNotFoundException;
import io.watch.user.repository.DepartmentRepository;
import io.watch.user.repository.UserRepository;
import io.watch.user.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        if (departmentRepository.existsByName(departmentDTO.getName())) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Department department = new Department();
        mapToEntity(departmentDTO, department);
        Department savedDepartment = departmentRepository.save(department);
        return mapToDTO(savedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDTO(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(UUID id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (!department.getName().equals(departmentDTO.getName()) && departmentRepository.existsByName(departmentDTO.getName())) {
            throw new IllegalArgumentException("Department name already exists");
        }

        mapToEntity(departmentDTO, department);
        Department updatedDepartment = departmentRepository.save(department);
        return mapToDTO(updatedDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(Department department) {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setId(department.getId());
        departmentDTO.setName(department.getName());
        departmentDTO.setDescription(department.getDescription());
        departmentDTO.setManagerId(department.getManager() != null ? department.getManager().getId() : null);
        departmentDTO.setCreatedAt(department.getCreatedAt());
        return departmentDTO;
    }

    private void mapToEntity(DepartmentDTO departmentDTO, Department department) {
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());

        if (departmentDTO.getManagerId() != null) {
            User manager = userRepository.findById(departmentDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + departmentDTO.getManagerId()));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }
    }
}