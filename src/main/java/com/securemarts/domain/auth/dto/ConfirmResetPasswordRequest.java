package com.securemarts.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Confirm password reset with email and 5-digit OTP code")
public class ConfirmResetPasswordRequest {

    @NotBlank
    @Schema(description = "Email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "5-digit OTP code from email", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
