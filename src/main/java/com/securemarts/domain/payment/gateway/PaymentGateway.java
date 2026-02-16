package com.securemarts.domain.payment.gateway;

import java.math.BigDecimal;

/**
 * Abstraction for Nigerian payment gateways (Paystack, Flutterwave, etc.).
 */
public interface PaymentGateway {

    String name();

    /**
     * Initialize a payment and return authorization URL or reference for the client.
     */
    InitResult initiate(InitRequest request);

    /**
     * Verify a transaction by gateway reference.
     */
    VerifyResult verify(String gatewayReference);

    /**
     * Request refund for a successful payment.
     */
    RefundResult refund(String gatewayReference, BigDecimal amount, String reason);

    @lombok.Data
    @lombok.Builder
    class InitRequest {
        private String email;
        private BigDecimal amount;
        private String currency;
        private String reference;
        private String callbackUrl;
        private String metadata; // JSON
    }

    @lombok.Data
    @lombok.Builder
    class InitResult {
        private boolean success;
        private String gatewayReference;
        private String authorizationUrl;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    class VerifyResult {
        private boolean success;
        private String gatewayReference;
        private String status;
        private BigDecimal amount;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    class RefundResult {
        private boolean success;
        private String refundReference;
        private String message;
    }
}
