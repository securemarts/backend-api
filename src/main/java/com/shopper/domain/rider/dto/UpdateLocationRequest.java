package com.shopper.domain.rider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update delivery location (tracking)")
public class UpdateLocationRequest {

    @Schema(description = "Latitude")
    private BigDecimal latitude;

    @Schema(description = "Longitude")
    private BigDecimal longitude;

    @Schema(description = "Optional note")
    private String note;
}
