package com.securemarts.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "Email", example = "merchant@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
