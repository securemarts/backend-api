package com.securemarts.domain.logistics.dto;

import com.securemarts.domain.logistics.entity.DeliveryOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Delivery order (linked to order)")
public class DeliveryOrderResponse {

    private String publicId;
    private String orderId; // order publicId - resolved in service
    private Long storeId;
    private String originHubPublicId;
    private String destinationHubPublicId;
    private String pickupAddress;
    private String deliveryAddress;
    @Schema(description = "Delivery status", allowableValues = {"PENDING", "ASSIGNED", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "FAILED", "RETURNED"})
    private String status;
    private String riderPublicId;
    private BigDecimal pricingAmount;
    private String pricingCurrency;
    private Instant scheduledAt;
    private Instant deliveredAt;
    private String failedReason;
    private String batchId;
    private int version;
    private Instant createdAt;

    public static DeliveryOrderResponse from(DeliveryOrder d) {
        return DeliveryOrderResponse.builder()
                .publicId(d.getPublicId())
                .storeId(d.getStoreId())
                .originHubPublicId(d.getOriginHub() != null ? d.getOriginHub().getPublicId() : null)
                .destinationHubPublicId(d.getDestinationHub() != null ? d.getDestinationHub().getPublicId() : null)
                .pickupAddress(d.getPickupAddress())
                .deliveryAddress(d.getDeliveryAddress())
                .status(d.getStatus().name())
                .riderPublicId(d.getRider() != null ? d.getRider().getPublicId() : null)
                .pricingAmount(d.getPricingAmount())
                .pricingCurrency(d.getPricingCurrency())
                .scheduledAt(d.getScheduledAt())
                .deliveredAt(d.getDeliveredAt())
                .failedReason(d.getFailedReason())
                .batchId(d.getBatchId())
                .version(d.getVersion())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
