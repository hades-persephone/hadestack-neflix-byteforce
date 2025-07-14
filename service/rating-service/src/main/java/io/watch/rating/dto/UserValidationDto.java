package io.watch.rating.dto;

import lombok.Data;

@Data
public class UserValidationDto {
    private String username;
    private String displayName;
    private String email;
    private boolean exists;
    private boolean active;
    private String role;
}
