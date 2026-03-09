package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Request to create a new plan")
public class CreatePlanRequest {

    @NotBlank
    @Schema(description = "Display name", example = "Starter", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Unique code (e.g. STARTER, PRO)", example = "STARTER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Optional description")
    private String description;

    @Schema(description = "Billing cycle", example = "MONTHLY", allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"})
    private String billingCycle = "MONTHLY";

    @Schema(description = "Currency code", example = "NGN")
    private String currency = "NGN";

    @NotNull
    @Schema(description = "Price amount", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceAmount;

    @Schema(description = "Status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status = "ACTIVE";

    @Schema(description = "Plan features (toggles and limits)")
    private List<@Valid PlanFeatureInput> features;

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlanFeatureInput {
        private String featureKey;
        private boolean enabled = true;
        private Integer limitValue;
    }
}
