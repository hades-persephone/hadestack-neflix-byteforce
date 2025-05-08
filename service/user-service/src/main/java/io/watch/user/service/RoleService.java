package io.watch.user.service;

import io.watch.user.dto.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDTO createRole(RoleDTO roleDTO);
    RoleDTO getRoleById(UUID id);
    List<RoleDTO> getAllRoles();
    RoleDTO updateRole(UUID id, RoleDTO roleDTO);
    void deleteRole(UUID id);
}
