package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Create delivery order for an order (Chowdeck model: customer location required for zone check and fee)")
public class CreateDeliveryOrderRequest {

    @NotBlank
    @Schema(description = "Order public ID", example = "39e7290c-1e40-4424-82fc-af93ebe5ab06", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderPublicId;

    @Schema(description = "Shipment ID (optional, for multi-shipment orders)")
    private Long shipmentId;

    @Schema(description = "Pickup address (defaults to store address)", example = "12 Broad Street, Lagos Island, Lagos")
    private String pickupAddress;

    @NotBlank
    @Schema(description = "Customer delivery address", example = "15 Awolowo Road, Ikoyi, Lagos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deliveryAddress;

    @NotNull
    @Schema(description = "Customer delivery latitude", example = "6.4541", requiredMode = Schema.RequiredMode.REQUIRED)
    private java.math.BigDecimal deliveryLat;

    @NotNull
    @Schema(description = "Customer delivery longitude", example = "3.4233", requiredMode = Schema.RequiredMode.REQUIRED)
    private java.math.BigDecimal deliveryLng;

    @Schema(description = "Scheduled delivery time (ISO 8601)", example = "2026-03-14T10:00:00Z")
    private Instant scheduledAt;

    @Schema(description = "Auto-assign nearest rider (default true). If false, delivery is PENDING for riders to claim.", example = "true")
    private boolean autoAssign = true;
}
