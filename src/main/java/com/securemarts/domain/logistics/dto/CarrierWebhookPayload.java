package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Carrier webhook payload: external shipment id and partner status")
public class CarrierWebhookPayload {

    @NotBlank
    @Schema(description = "Partner's shipment/shipment ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalShipmentId;

    @Schema(description = "Partner status (e.g. PICKED_UP, IN_TRANSIT, DELIVERED, FAILED). Mapped to DeliveryStatus.")
    private String status;

    private String trackingUrl;
}
