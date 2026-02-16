package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Set permissions for a role (replaces existing)")
public class AssignPermissionsToRoleRequest {

    @NotNull
    @Schema(description = "Permission codes to assign", example = "[\"business:list\", \"admin:read\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionCodes;
}
