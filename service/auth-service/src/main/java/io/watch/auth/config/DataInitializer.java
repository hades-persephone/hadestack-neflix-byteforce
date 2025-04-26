package io.watch.auth.config;

import io.watch.auth.entity.Permission;
import io.watch.auth.entity.Role;
import io.watch.auth.entity.User;
import io.watch.auth.repository.PermissionRepository;
import io.watch.auth.repository.RoleRepository;
import io.watch.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initializes the database with default roles, permissions, and users.
 */
//@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing database with default roles and permissions");
        
        // Create permissions if they don't exist
        createPermissionsIfNotExist();
        
        // Create roles if they don't exist
        createRolesIfNotExist();
        
        // Create admin user if it doesn't exist
        createAdminUserIfNotExist();
        
        logger.info("Database initialization completed");
    }
    
    private void createPermissionsIfNotExist() {
        List<String> permissionNames = Arrays.asList(
                "user:read", "user:write", "user:delete",
                "profile:read", "profile:write", "profile:delete",
                "movie:read", "movie:write", "movie:delete",
                "admin:read", "admin:write", "admin:delete"
        );
        
        for (String name : permissionNames) {
            if (!permissionRepository.existsByName(name)) {
                Permission permission = new Permission();
                permission.setName(name);
                permission.setDescription("Permission to " + name.replace(":", " "));
                permissionRepository.save(permission);
                logger.info("Created permission: {}", name);
            }
        }
    }
    
    private void createRolesIfNotExist() {
        // Create USER role
        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Regular user role");
            
            // Add permissions to USER role
            Set<Permission> userPermissions = new HashSet<>(permissionRepository.findAllById(Arrays.asList(
                    permissionRepository.findByName("user:read").get().getId(),
                    permissionRepository.findByName("profile:read").get().getId(),
                    permissionRepository.findByName("profile:write").get().getId(),
                    permissionRepository.findByName("movie:read").get().getId()
            )));
            userRole.setPermissions(userPermissions);
            
            roleRepository.save(userRole);
            logger.info("Created role: USER");
        }
        
        // Create ADMIN role
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            
            // Add all permissions to ADMIN role
            Set<Permission> adminPermissions = new HashSet<>(permissionRepository.findAll());
            adminRole.setPermissions(adminPermissions);
            
            roleRepository.save(adminRole);
            logger.info("Created role: ADMIN");
        }
    }
    
    private void createAdminUserIfNotExist() {
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@example.com");
            adminUser.setFullName("System Administrator");
            adminUser.setEnabled(true);
            
            // Add ADMIN role to admin user
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            adminUser.addRole(adminRole);
            
            userRepository.save(adminUser);
            logger.info("Created admin user: admin");
        }
    }
}