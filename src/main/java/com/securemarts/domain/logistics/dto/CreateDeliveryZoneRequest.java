package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create delivery zone")
public class CreateDeliveryZoneRequest {

    private String hubPublicId;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String state;

    @Size(max = 100)
    private String city;

    private BigDecimal radiusKm;

    private boolean active = true;
}
