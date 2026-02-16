package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create admin request (superuser only)")
public class CreateAdminRequest {

    @NotBlank
    @Email
    @Schema(description = "Admin email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;

    @NotBlank
    @Schema(description = "Full name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Single admin role (use when not providing roles list)", allowableValues = {"SUPERUSER", "PLATFORM_ADMIN", "SUPPORT"})
    private String role;

    @Schema(description = "Multiple roles (RBAC). If provided, overrides role. Values: SUPERUSER, PLATFORM_ADMIN, SUPPORT")
    private List<String> roles;
}
