package io.watch.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token validation requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationRequest {
    
    @NotBlank(message = "Token cannot be blank")
    private String token;
}