package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update delivery route")
public class UpdateDeliveryRouteRequest {

    private BigDecimal estimatedHours;

    private Boolean active;
}
