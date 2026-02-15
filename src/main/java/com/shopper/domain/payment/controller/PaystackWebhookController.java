package com.shopper.domain.payment.controller;

import com.shopper.domain.payment.service.PaystackWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Paystack webhook endpoint. No auth; signature verification is performed inside.
 */
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Payment gateway webhooks (Paystack)")
public class PaystackWebhookController {

    private final PaystackWebhookService paystackWebhookService;

    @PostMapping("/paystack")
    @Operation(summary = "Paystack webhook", description = "Receives subscription and charge events. Verify x-paystack-signature before processing.")
    public ResponseEntity<Void> paystack(
            @RequestBody String rawBody,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!paystackWebhookService.verifySignature(rawBody, signature)) {
            return ResponseEntity.status(401).build();
        }
        paystackWebhookService.handleEvent(rawBody);
        return ResponseEntity.ok().build();
    }
}
