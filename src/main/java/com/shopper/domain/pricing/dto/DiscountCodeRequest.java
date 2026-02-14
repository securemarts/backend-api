package com.shopper.domain.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create discount code for a price rule")
public class DiscountCodeRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Code (e.g. SAVE10)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Usage limit for this code (null = use rule limit)")
    private Integer usageLimit;
}
