package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Create delivery order for an order (Chowdeck model: customer location required for zone check and fee)")
public class CreateDeliveryOrderRequest {

    @NotBlank
    private String orderPublicId;

    private String pickupAddress;

    @NotBlank
    private String deliveryAddress;

    /** Customer delivery coordinates (required for zone check, fee calculation, and dispatch) */
    @NotNull
    private java.math.BigDecimal deliveryLat;

    @NotNull
    private java.math.BigDecimal deliveryLng;

    private Instant scheduledAt;

    /** If false, delivery is created as PENDING for riders to claim; if true (default), nearest rider is auto-assigned. */
    private boolean autoAssign = true;
}
