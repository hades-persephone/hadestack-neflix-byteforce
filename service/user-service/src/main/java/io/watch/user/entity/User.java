package io.watch.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.user.entity.substraction.AccountStatus;
import io.watch.user.entity.substraction.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
@Schema(description = "User entity representing a system user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true)
    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true)
    @Schema(description = "Email address of the user", example = "john@example.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Column(name = "password", nullable = false)
    @Schema(description = "Hashed password of the user", example = "hashed_password_1")
    private String password;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    @Column(name = "full_name")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    @Schema(description = "Date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @Schema(description = "Gender of the user", example = "MALE")
    private Gender gender;

    @Size(max = 50, message = "Country cannot exceed 50 characters")
    @Column(name = "country")
    @Schema(description = "Country of the user", example = "USA")
    private String country;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone_number")
    @Schema(description = "Phone number", example = "123-456-7890")
    private String phoneNumber;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    @Column(name = "profile_picture_url")
    @Schema(description = "URL of profile picture", example = "https://example.com/pic.jpg")
    private String profilePictureUrl;

    @Size(max = 50, message = "Subscription plan cannot exceed 50 characters")
    @Column(name = "subscription_plan")
    @Schema(description = "Subscription plan", example = "Premium")
    private String subscriptionPlan;

    @Column(name = "subscription_start_date")
    @Schema(description = "Start date of subscription", example = "2025-01-01")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    @Schema(description = "End date of subscription", example = "2025-12-31")
    private LocalDate subscriptionEndDate;

    @Column(name = "last_login")
    @Schema(description = "Last login timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Size(max = 10, message = "Preferred language cannot exceed 10 characters")
    @Column(name = "preferred_language")
    @Schema(description = "Preferred language", example = "en")
    private String preferredLanguage;

    @Column(name = "notification_enabled")
    @Schema(description = "Notification enabled flag", example = "true")
    private Boolean notificationEnabled = true;

    @Column(name = "created_at")
    @CreationTimestamp
    @Schema(description = "Creation timestamp", example = "2025-04-10T10:00:00Z")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Schema(description = "Update timestamp", example = "2025-04-10T12:00:00Z")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @Schema(description = "Deletion timestamp", example = "null")
    private LocalDateTime deletedAt;

    @Column(name = "created_by")
    @Schema(description = "ID of user who created this record", example = "1")
    private Long createdBy;

    @Column(name = "updated_by")
    @Schema(description = "ID of user who updated this record", example = "1")
    private Long updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @Schema(description = "Department the user belongs to")
    private Department department;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Schema(description = "Roles assigned to the user")
    private Set<Role> roles = new HashSet<>();
}