package com.securemarts.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request password reset")
public class ResetPasswordRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
