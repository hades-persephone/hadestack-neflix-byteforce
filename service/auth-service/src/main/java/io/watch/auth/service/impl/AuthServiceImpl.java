package io.watch.auth.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.watch.auth.dto.AuthResponse;
import io.watch.auth.dto.LoginRequest;
import io.watch.auth.dto.RefreshTokenRequest;
import io.watch.auth.dto.RegisterRequest;
import io.watch.auth.entity.Permission;
import io.watch.auth.entity.RefreshToken;
import io.watch.auth.entity.Role;
import io.watch.auth.entity.User;
import io.watch.auth.entity.substraction.AccountStatus;
import io.watch.auth.repository.RefreshTokenRepository;
import io.watch.auth.repository.RoleRepository;
import io.watch.auth.repository.UserRepository;
import io.watch.auth.service.AuthService;
import io.watch.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WebClient userServiceWebClient;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "loginFallback")
    public Mono<AuthResponse> login(LoginRequest loginRequest) {
        return validateUser(loginRequest.getUsername(), loginRequest.getPassword())
                .flatMap(user -> {
                    List<String> roles = user.getRoles().stream()
                            .map(Role::getName)
                            .toList();

                    List<String> permissions = user.getRoles().stream()
                            .flatMap(role -> role.getPermissions().stream())
                            .map(Permission::getName)
                            .distinct()
                            .toList();

                    String accessToken = jwtService.generateToken(user.getUsername(), user.getId(), roles, permissions);
                    String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getId(), roles, permissions);

                    RefreshToken reToken = new RefreshToken();
                    reToken.setToken(refreshToken);
                    reToken.setUser(user);
                    reToken.setExpiryDate(LocalDateTime.now().plusDays(7));

                    // Save refresh token in a reactive context
                    return Mono.fromCallable(() -> refreshTokenRepository.save(reToken))
                            .subscribeOn(Schedulers.boundedElastic())
                            .then(Mono.just(buildAuthResponse(user, accessToken, refreshToken, 900_000L))); // 15 minutes
                })
                .doOnSuccess(response -> logger.info("User logged in: username={}, userId={}",
                        response.getUsername(), response.getUserId()))
                .doOnError(e -> logger.error("Login failed for username={}: {}", loginRequest.getUsername(), e.getMessage()));
    }


    @Override
    @Transactional
    public Mono<AuthResponse> refreshToken(RefreshTokenRequest request) {
        return Mono.fromCallable(() -> refreshTokenRepository.findByToken(request.getRefreshToken()))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")))
                .flatMap(refreshToken -> {
                    if (refreshToken.isEmpty()) {
                        return Mono.error(new RuntimeException("Invalid refresh token"));
                    }
                    if (refreshToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
                        return Mono.fromRunnable(() -> refreshTokenRepository.delete(refreshToken.get()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .then(Mono.error(new RuntimeException("Refresh token expired")));
                    }

                    return validateUserById(refreshToken.get().getUser().getId())
                            .flatMap(user -> {
                                List<String> roles = user.getRoles().stream()
                                        .map(Role::getName)
                                        .toList();

                                List<String> permissions = user.getRoles().stream()
                                        .flatMap(role -> role.getPermissions().stream())
                                        .map(Permission::getName)
                                        .distinct()
                                        .toList();

                                String newAccessToken = jwtService.generateToken(user.getUsername(), user.getId(), roles, permissions);
                                String newRefreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getId(), roles, permissions);

                                // Delete old refresh token and save new one
                                RefreshToken newReToken = new RefreshToken();
                                newReToken.setToken(newRefreshToken);
                                newReToken.setUser(user);
                                newReToken.setExpiryDate(LocalDateTime.now().plusDays(7));

                                return Mono.fromCallable(() -> {
                                            refreshTokenRepository.delete(refreshToken.get());
                                            refreshTokenRepository.save(newReToken);
                                            return null;
                                        })
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .then(Mono.just(buildAuthResponse(user, newAccessToken, newRefreshToken, 900_000L))); // 15 minutes
                            })
                            .doOnSuccess(response -> logger.info("Token refreshed: userId={}", response.getUserId()))
                            .doOnError(e -> logger.error("Refresh token failed: {}", e.getMessage()));
                });
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if the username or email already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create a new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());

        if (user.getAccountStatus() == null) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        user.setRoles(Collections.synchronizedSet(new HashSet<>()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.addRole(userRole);

        // Save the user
        user = userRepository.save(user);

        // Get the roles and permissions
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .collect(Collectors.toList());
        
        // Generate the JWT token
        String token = jwtService.generateToken(user.getUsername(), user.getId(), roles, permissions);
        
        // Create and return the AuthResponse
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .token(token)
                .roles(roles)
                .permissions(permissions)
                .build();
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return username != null && !jwtService.isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public AuthResponse getCurrentUser() {
        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Get the username from the authentication
        String username = authentication.getName();
        
        // Get the user from the repository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // Get the roles and permissions
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .collect(Collectors.toList());
        
        // Create and return the AuthResponse (without generating a new token)
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private Mono<User> validateUser(String username, String password) {
        return userServiceWebClient.get()
                .uri("/users/username/{username}", username)
                .retrieve()
                .bodyToMono(User.class)
                .flatMap(user -> {
                    if (user.getPassword().equals(password)) {
                        return Mono.just(user);
                    }
                    return Mono.error(new RuntimeException("Invalid credentials"));
                });
    }

    private Mono<User> validateUserById(UUID userId) {
        return userServiceWebClient.get()
                .uri("/users/{userId}", userId)
                .retrieve()
                .bodyToMono(User.class);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken, long expiresIn) {
        AuthResponse response = new AuthResponse();
        response.setToken(accessToken);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        return response;
    }

    private Mono<AuthResponse> loginFallback(LoginRequest request, Throwable t) {
        logger.error("Circuit breaker fallback for login: username={}, error={}", request.getUsername(), t.getMessage());
        return Mono.error(new RuntimeException("Authentication service unavailable"));
    }
}