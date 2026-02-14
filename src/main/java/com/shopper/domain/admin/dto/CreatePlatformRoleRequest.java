package com.shopper.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create platform role")
public class CreatePlatformRoleRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "Unique code, e.g. CUSTOM_ROLE", example = "CUSTOM_ROLE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 100)
    @Schema(description = "Display name")
    private String name;

    @Size(max = 255)
    @Schema(description = "Description")
    private String description;
}
