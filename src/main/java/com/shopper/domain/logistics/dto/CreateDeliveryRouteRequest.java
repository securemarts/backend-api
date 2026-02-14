package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create delivery route")
public class CreateDeliveryRouteRequest {

    @NotBlank
    private String originHubPublicId;

    @NotBlank
    private String destinationHubPublicId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal estimatedHours;

    private boolean active = true;
}
