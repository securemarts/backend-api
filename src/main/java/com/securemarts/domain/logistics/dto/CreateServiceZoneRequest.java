package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create service zone (Chowdeck: radius-based, base_fee + per_km_fee)")
public class CreateServiceZoneRequest {

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    private String city;

    @NotNull
    @DecimalMin("-90")
    private BigDecimal centerLat;

    @NotNull
    @DecimalMin("-180")
    private BigDecimal centerLng;

    @NotNull
    @DecimalMin("0")
    private BigDecimal radiusKm;

    @NotNull
    @DecimalMin("0")
    private BigDecimal baseFee;

    @NotNull
    @DecimalMin("0")
    private BigDecimal perKmFee;

    private BigDecimal maxDistanceKm;
    private BigDecimal minOrderAmount;
    private boolean surgeEnabled = false;
    private boolean active = true;
}
