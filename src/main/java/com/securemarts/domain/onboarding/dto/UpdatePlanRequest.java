package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Request to update a plan")
public class UpdatePlanRequest {

    @Schema(description = "Display name")
    private String name;

    @Schema(description = "Optional description")
    private String description;

    @Schema(description = "Billing cycle", allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"})
    private String billingCycle;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Price amount")
    private BigDecimal priceAmount;

    @Schema(description = "Status", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Schema(description = "Plan features (replaces existing)")
    private List<CreatePlanRequest.PlanFeatureInput> features;
}
