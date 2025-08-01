package io.watch.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for login requests.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @NotBlank(message = "Password cannot be blank")
    private String password;
}