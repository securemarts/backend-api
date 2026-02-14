package com.shopper.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create admin permission")
public class CreateAdminPermissionRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    @Schema(description = "Unique code, e.g. business:list", example = "business:list", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 255)
    @Schema(description = "Description")
    private String description;
}
