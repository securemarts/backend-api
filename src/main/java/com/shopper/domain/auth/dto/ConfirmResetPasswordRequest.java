package com.shopper.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Confirm password reset with token")
public class ConfirmResetPasswordRequest {

    @NotBlank
    @Schema(description = "Reset token from email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
