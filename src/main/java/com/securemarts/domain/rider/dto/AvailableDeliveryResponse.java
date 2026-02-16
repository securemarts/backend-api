package com.securemarts.domain.rider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "PENDING delivery available for rider to claim (within radius)")
public class AvailableDeliveryResponse {

    private String publicId;
    private String orderId;
    private String storeId;
    private String pickupAddress;
    private String deliveryAddress;
    @Schema(description = "Delivery status (PENDING for available list)", allowableValues = {"PENDING", "ASSIGNED", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "FAILED", "RETURNED"})
    private String status;
    private BigDecimal pricingAmount;
    private String pricingCurrency;
    private Instant scheduledAt;
    private Instant createdAt;
    /** Distance in km from rider's position to pickup (store). */
    private Double distanceKm;
}
