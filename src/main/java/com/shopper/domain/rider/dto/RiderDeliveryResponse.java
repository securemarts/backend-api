package com.shopper.domain.rider.dto;

import com.shopper.domain.logistics.entity.DeliveryOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Delivery order for rider app")
public class RiderDeliveryResponse {

    private String publicId;
    private String orderId;
    private String storeId; // store publicId for display
    private String pickupAddress;
    private String deliveryAddress;
    private String status;
    private BigDecimal pricingAmount;
    private String pricingCurrency;
    private Instant scheduledAt;
    private int version;
    private Instant createdAt;
}
