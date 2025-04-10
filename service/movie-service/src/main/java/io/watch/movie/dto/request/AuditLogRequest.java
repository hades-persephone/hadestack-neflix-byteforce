package io.watch.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.ActionType;
import io.watch.movie.util.ValidEnum;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating an audit log")
public class AuditLogRequest {

    @NotBlank(message = "Entity name cannot be blank")
    @Size(max = 50, message = "Entity name cannot exceed 50 characters")
    @Schema(description = "Name of the entity affected", example = "movies")
    private String entityName;

    @NotNull(message = "Entity ID cannot be null")
    @Schema(description = "ID of the affected entity", example = "1")
    private Long entityId;

    @NotNull(message = "Action must not be null")
    @ValidEnum(enumClass = ActionType.class, message = "Invalid action")
    @Schema(description = "Action performed", example = "CREATE")
    private ActionType action;

    @Schema(description = "ID of the user who performed the action", example = "1")
    private Long actionBy;

    @Schema(description = "Old value before the action (JSON)", example = "{}")
    private String oldValue;

    @Schema(description = "New value after the action (JSON)", example = "{\"title\": \"Inception\"}")
    private String newValue;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Schema(description = "IP address of the requester", example = "192.168.1.1")
    private String ipAddress;

    @Size(max = 255, message = "User agent cannot exceed 255 characters")
    @Schema(description = "User agent of the requester", example = "Mozilla/5.0")
    private String userAgent;
}
