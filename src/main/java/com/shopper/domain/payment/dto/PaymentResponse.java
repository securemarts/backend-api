package com.shopper.domain.payment.dto;

import com.shopper.domain.payment.entity.PaymentTransaction;
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
    private String status;
    private BigDecimal amount;
    private String currency;
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
