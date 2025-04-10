package io.watch.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.watch.movie.entity.substraction.ActionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload for audit log")
public class AuditLogResponse {

    @Schema(description = "Unique identifier of the audit log", example = "1")
    private UUID id;

    @Schema(description = "Name of the entity affected", example = "movies")
    private String entityName;

    @Schema(description = "ID of the affected entity", example = "1")
    private Long entityId;

    @Schema(description = "Action performed", example = "CREATE")
    private ActionType action;

    @Schema(description = "ID of the user who performed the action", example = "1")
    private Long actionBy;

    @Schema(description = "Timestamp of the action", example = "2025-04-10T10:00:00Z")
    private LocalDateTime actionAt;

    @Schema(description = "Old value before the action (JSON)", example = "{}")
    private String oldValue;

    @Schema(description = "New value after the action (JSON)", example = "{\"title\": \"Inception\"}")
    private String newValue;

    @Schema(description = "IP address of the requester", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent of the requester", example = "Mozilla/5.0")
    private String userAgent;
}
