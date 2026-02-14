package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Set permissions for a merchant role")
public class AssignMerchantPermissionsToRoleRequest {

    @NotNull
    @Schema(description = "Permission codes to assign", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionCodes;
}
