package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create delivery pricing rule (zone-based or route-based)")
public class CreateDeliveryPricingRuleRequest {

    private String zonePublicId;
    private String originHubPublicId;
    private String destinationHubPublicId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal baseAmount;

    @DecimalMin("0")
    private BigDecimal perKgAmount;

    @DecimalMin("0")
    private BigDecimal sameCityMultiplier;

    private boolean active = true;
}
