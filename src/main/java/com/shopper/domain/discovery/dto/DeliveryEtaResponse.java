package com.shopper.domain.discovery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Estimated delivery time by location")
public class DeliveryEtaResponse {

    @Schema(description = "Estimated hours from now")
    private BigDecimal estimatedHours;

    @Schema(description = "Resolved hub or zone name (if applicable)")
    private String zoneOrHubName;
}
