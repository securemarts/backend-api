package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create merchant role")
public class CreateMerchantRoleRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "Unique code, e.g. CASHIER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
