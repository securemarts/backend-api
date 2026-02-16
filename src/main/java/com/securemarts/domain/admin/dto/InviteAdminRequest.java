package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Invite admin by email (superuser only). Invitee completes setup with token + password.")
public class InviteAdminRequest {

    @NotBlank
    @Email
    @Schema(description = "Email to invite", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Full name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotNull
    @Schema(description = "Admin role", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"SUPERUSER", "PLATFORM_ADMIN", "SUPPORT"})
    private String role;
}
