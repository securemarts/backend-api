package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update service zone")
public class UpdateServiceZoneRequest {

    private String name;
    private String city;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private BigDecimal radiusKm;
    private BigDecimal baseFee;
    private BigDecimal perKmFee;
    private BigDecimal maxDistanceKm;
    private BigDecimal minOrderAmount;
    private Boolean surgeEnabled;
    private Boolean active;
}
