package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create merchant permission")
public class CreateMerchantPermissionRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    @Schema(description = "Unique code, e.g. products:read", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 255)
    private String description;
}
