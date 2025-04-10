package io.watch.movie.dto.mapper;

import io.watch.movie.dto.request.AuditLogRequest;
import io.watch.movie.dto.response.AuditLogResponse;
import io.watch.movie.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLog toEntity(AuditLogRequest request);
    AuditLogResponse toResponse(AuditLog entity);
}

