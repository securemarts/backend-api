package com.securemarts.domain.logistics.partner;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShipmentRequest {

    private String orderPublicId;
    private Long shipmentId;
    private Long storeId;
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
    private String deliveryAddress;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;
    private String parcelDescription;
    private BigDecimal weightKg;
}
