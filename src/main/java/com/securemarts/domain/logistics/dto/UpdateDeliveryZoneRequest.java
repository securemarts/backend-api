package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update delivery zone")
public class UpdateDeliveryZoneRequest {

    private String hubPublicId;

    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String state;

    @Size(max = 100)
    private String city;

    private BigDecimal radiusKm;

    private Boolean active;
}
