package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current admin identity and RBAC: roles and permission scopes")
public class AdminMeResponse {

    @Schema(description = "Admin public ID")
    private String publicId;

    @Schema(description = "Admin email")
    private String email;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Admin roles (e.g. SUPERUSER, PLATFORM_ADMIN, SUPPORT)")
    private List<String> roles;

    @Schema(description = "Permission codes (scopes) for UI to show/hide features, e.g. business:list, admin:create")
    private List<String> scopes;
}
