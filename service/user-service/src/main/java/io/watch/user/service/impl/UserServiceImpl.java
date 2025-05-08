package io.watch.user.service.impl;

import io.watch.user.dto.UserDTO;
import io.watch.user.entity.Department;
import io.watch.user.entity.Role;
import io.watch.user.entity.User;
import io.watch.user.exception.ResourceNotFoundException;
import io.watch.user.repository.DepartmentRepository;
import io.watch.user.repository.RoleRepository;
import io.watch.user.repository.UserRepository;
import io.watch.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        mapToEntity(userDTO, user);
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(userDTO.getUsername()) && userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        mapToEntity(userDTO, user);
        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setDateOfBirth(user.getDateOfBirth());
        userDTO.setGender(user.getGender());
        userDTO.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
        userDTO.setRoleIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toSet()));
        userDTO.setAccountStatus(user.getAccountStatus());
        userDTO.setCreatedAt(user.getCreatedAt());
        return userDTO;
    }

    private void mapToEntity(UserDTO userDTO, User user) {
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setGender(userDTO.getGender());
        user.setAccountStatus(userDTO.getAccountStatus());

        if (userDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + userDTO.getDepartmentId()));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }

        if (userDTO.getRoleIds() != null) {
            Set<Role> roles = userDTO.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
    }
}