package io.watch.user.service;

import io.watch.user.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    void deleteUser(UUID id);
    UserDTO updateUser(UUID id, UserDTO userDTO);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(UUID id);
}
