package com.securemarts.domain.logistics.controller;

import com.securemarts.domain.logistics.dto.CarrierWebhookPayload;
import com.securemarts.domain.logistics.service.CarrierWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook endpoint for external carriers (Aramex, Sendstack, etc.). No auth; secure by URL or partner signature in future.
 */
@RestController
@RequestMapping("/webhooks/carrier")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Carrier status webhooks")
public class CarrierWebhookController {

    private final CarrierWebhookService carrierWebhookService;

    @PostMapping("/{carrierCode}")
    @Operation(summary = "Carrier status webhook", description = "Receive status update from carrier. Body: externalShipmentId, status (PENDING|PICKED_UP|IN_TRANSIT|DELIVERED|FAILED|RETURNED), optional trackingUrl.")
    public ResponseEntity<Void> carrierStatus(
            @PathVariable String carrierCode,
            @Valid @RequestBody CarrierWebhookPayload payload) {
        carrierWebhookService.handleStatusUpdate(carrierCode.toUpperCase(), payload);
        return ResponseEntity.ok().build();
    }
}
