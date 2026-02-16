package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Admin invite response (invite token for completion link)")
public class AdminInviteResponse {

    private String publicId;
    private String email;
    private String inviteToken;
    private Instant expiresAt;
    private Instant createdAt;

    @Schema(description = "URL path for invitee to complete setup: POST /admin/auth/complete-setup with inviteToken, email, password")
    private String completeSetupPath;
}
