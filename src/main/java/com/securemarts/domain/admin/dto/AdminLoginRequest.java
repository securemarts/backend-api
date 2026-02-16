package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Admin login request")
public class AdminLoginRequest {

    @NotBlank
    @Email
    @Schema(description = "Admin email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
