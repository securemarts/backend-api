package com.securemarts.domain.logistics.partner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentResult {

    private boolean success;
    private String externalShipmentId;
    private String trackingUrl;
    private String labelUrl;
    private String message;
}
