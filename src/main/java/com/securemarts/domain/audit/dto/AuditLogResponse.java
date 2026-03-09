package com.securemarts.domain.audit.dto;

import com.securemarts.domain.audit.entity.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single audit log entry for list/export")
public class AuditLogResponse {

    @Schema(description = "Activity ID (public ID)")
    private String activityId;

    @Schema(description = "User label or identifier")
    private String user;

    @Schema(description = "Action performed")
    private String action;

    @Schema(description = "Module affected")
    private String module;

    @Schema(description = "When the action occurred")
    private Instant timestamp;

    @Schema(description = "IP address")
    private String ipAddress;

    public static AuditLogResponse from(AuditLog a) {
        String userLabel = a.getActorLabel() != null ? a.getActorLabel() : (a.getActorPublicId() != null ? a.getActorPublicId() : "-");
        return AuditLogResponse.builder()
                .activityId(a.getPublicId())
                .user(userLabel)
                .action(a.getAction())
                .module(a.getModule())
                .timestamp(a.getCreatedAt())
                .ipAddress(a.getIpAddress())
                .build();
    }
}
