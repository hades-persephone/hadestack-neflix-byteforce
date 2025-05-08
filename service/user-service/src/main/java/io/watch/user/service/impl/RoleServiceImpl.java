package io.watch.user.service.impl;

import io.watch.user.dto.RoleDTO;
import io.watch.user.entity.Permission;
import io.watch.user.entity.Role;
import io.watch.user.exception.ResourceNotFoundException;
import io.watch.user.repository.PermissionRepository;
import io.watch.user.repository.RoleRepository;
import io.watch.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }

        Role role = new Role();
        mapToEntity(roleDTO, role);
        Role savedRole = roleRepository.save(role);
        return mapToDTO(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return mapToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO updateRole(UUID id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (!role.getName().equals(roleDTO.getName()) && roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }

        mapToEntity(roleDTO, role);
        Role updatedRole = roleRepository.save(role);
        return mapToDTO(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    private RoleDTO mapToDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
        roleDTO.setDescription(role.getDescription());
        roleDTO.setPermissionIds(role.getPermissions().stream().map(Permission::getId).collect(Collectors.toSet()));
        roleDTO.setCreatedAt(role.getCreatedAt());
        return roleDTO;
    }

    private void mapToEntity(RoleDTO roleDTO, Role role) {
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());

        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = roleDTO.getPermissionIds().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        } else {
            role.setPermissions(null);
        }
    }
}