package com.securemarts.domain.payment.controller;

import com.securemarts.domain.payment.dto.InitiatePaymentRequest;
import com.securemarts.domain.payment.dto.PaymentResponse;
import com.securemarts.domain.payment.service.PaymentService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storePublicId}/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Initiate, verify, refund (Paystack/Flutterwave)")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final StoreRepository storeRepository;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment", description = "Returns authorization URL for Paystack/Flutterwave")
    public ResponseEntity<PaymentResponse> initiate(
            @PathVariable String storePublicId,
            @Valid @RequestBody InitiatePaymentRequest request) {
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(paymentService.initiate(storeId, request));
    }

    @PostMapping("/{paymentPublicId}/verify")
    @Operation(summary = "Verify payment", description = "Call after user returns from gateway")
    public ResponseEntity<PaymentResponse> verify(
            @PathVariable String storePublicId,
            @PathVariable String paymentPublicId) {
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(paymentService.verify(storeId, paymentPublicId));
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }
}
