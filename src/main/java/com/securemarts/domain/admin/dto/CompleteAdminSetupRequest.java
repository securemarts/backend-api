package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Complete admin onboarding: supply invite token, email, and password (no auth required)")
public class CompleteAdminSetupRequest {

    @NotBlank
    @Schema(description = "Invite token from invite email/link", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inviteToken;

    @NotBlank
    @Email
    @Schema(description = "Email (must match invited email)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Password to set", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;
}
