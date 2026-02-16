package com.securemarts.domain.payment.dto;

import com.securemarts.domain.payment.entity.PaymentTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Payment transaction response")
public class PaymentResponse {

    private String publicId;
    @Schema(description = "Payment status", allowableValues = {"PENDING", "INITIATED", "SUCCESS", "FAILED", "CANCELLED", "REFUNDED"})
    private String status;
    private BigDecimal amount;
    private String currency;
    @Schema(description = "Payment gateway", allowableValues = {"PAYSTACK", "FLUTTERWAVE"})
    private String gateway;
    private String gatewayReference;
    private String authorizationUrl;
    @Schema(description = "Order public ID (when payment was initiated with orderId)")
    private String orderPublicId;
    private Instant createdAt;

    public static PaymentResponse from(PaymentTransaction t, String authorizationUrl, String orderPublicId) {
        return PaymentResponse.builder()
                .publicId(t.getPublicId())
                .status(t.getStatus().name())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .gateway(t.getGateway())
                .gatewayReference(t.getGatewayReference())
                .authorizationUrl(authorizationUrl)
                .orderPublicId(orderPublicId)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
