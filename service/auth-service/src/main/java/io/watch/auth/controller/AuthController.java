package io.watch.auth.controller;

import io.watch.auth.dto.AuthResponse;
import io.watch.auth.dto.LoginRequest;
import io.watch.auth.dto.RegisterRequest;
import io.watch.auth.dto.TokenValidationRequest;
import io.watch.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication requests.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint for user login.
     *
     * @param loginRequest the login request containing username and password
     * @return a response entity containing the JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    /**
     * Endpoint for user registration.
     *
     * @param registerRequest the registration request containing user details
     * @return a response entity containing the JWT token and user details
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    /**
     * Endpoint for validating a JWT token.
     *
     * @param request the token validation request containing the token
     * @return a response entity indicating whether the token is valid
     */
    @PostMapping("/token/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody TokenValidationRequest request) {
        return ResponseEntity.ok(authService.validateToken(request.getToken()));
    }

    /**
     * Endpoint for getting user details.
     *
     * @return a response entity containing the user details
     */
    @GetMapping("/user/me")
    public ResponseEntity<AuthResponse> getUserDetails() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    /**
     * Admin-only endpoint for demonstration purposes.
     *
     * @return a response entity containing a message
     */
    @GetMapping("/admin/dashboard")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome to the admin dashboard!");
    }
}