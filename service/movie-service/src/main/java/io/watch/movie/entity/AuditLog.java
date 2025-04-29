package io.watch.movie.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.ActionType;
import io.watch.movie.util.ValidEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "audit_logs")
@Schema(description = "Audit log entity to track changes to records")
public class AuditLog extends EntityBase {

    @NotBlank(message = "Entity name cannot be blank")
    @Size(max = 50, message = "Entity name cannot exceed 50 characters")
    @Column(name = "entity_name", nullable = false)
    @Schema(description = "Name of the entity affected", example = "movies")
    private String entityName;

    @NotNull(message = "Entity ID cannot be null")
    @Column(name = "entity_id", nullable = false)
    @Schema(description = "ID of the affected entity", example = "1")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    @Schema(description = "Action performed", example = "CREATE")
    @ValidEnum(enumClass = ActionType.class, message = "Invalid enum value")
    private ActionType action;

    @Column(name = "action_by")
    @Schema(description = "ID of the user who performed the action", example = "1")
    private Long actionBy;

    @Column(name = "action_at")
    @CreationTimestamp
    @Schema(description = "Timestamp of the action", example = "2025-04-10T10:00:00Z")
    private LocalDateTime actionAt;

    @Column(name = "old_value", columnDefinition = "JSONB")
    @Schema(description = "Old value before the action (JSON)", example = "{}")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSONB")
    @Schema(description = "New value after the action (JSON)", example = "{\"title\": \"Inception\"}")
    private String newValue;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Column(name = "ip_address")
    @Schema(description = "IP address of the requester", example = "192.168.1.1")
    private String ipAddress;

    @Size(max = 255, message = "User agent cannot exceed 255 characters")
    @Column(name = "user_agent")
    @Schema(description = "User agent of the requester", example = "Mozilla/5.0")
    private String userAgent;
}
