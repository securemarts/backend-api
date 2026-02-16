package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Update admin (superuser only)")
public class UpdateAdminRequest {

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Admin roles: SUPERUSER, PLATFORM_ADMIN, SUPPORT (replaces existing when provided)")
    private List<String> roles;

    @Schema(description = "Active status")
    private Boolean active;
}
