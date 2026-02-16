package com.securemarts.domain.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Flutterwave integration stub. Replace with real HTTP client and Flutterwave API.
 * Docs: https://developer.flutterwave.com/reference
 */
@Component
@Slf4j
public class FlutterwaveGateway implements PaymentGateway {

    @Value("${app.payment.flutterwave.secret-key:}")
    private String secretKey;

    @Override
    public String name() {
        return "FLUTTERWAVE";
    }

    @Override
    public PaymentGateway.InitResult initiate(PaymentGateway.InitRequest request) {
        log.info("Flutterwave initiate: ref={}, amount={}", request.getReference(), request.getAmount());
        if (secretKey == null || secretKey.isBlank()) {
            return PaymentGateway.InitResult.builder()
                    .success(false)
                    .message("Flutterwave not configured")
                    .build();
        }
        return PaymentGateway.InitResult.builder()
                .success(true)
                .gatewayReference("flutterwave_" + request.getReference())
                .authorizationUrl("https://checkout.flutterwave.com/stub/" + request.getReference())
                .build();
    }

    @Override
    public PaymentGateway.VerifyResult verify(String gatewayReference) {
        log.info("Flutterwave verify: {}", gatewayReference);
        return PaymentGateway.VerifyResult.builder()
                .success(true)
                .gatewayReference(gatewayReference)
                .status("successful")
                .amount(BigDecimal.ZERO)
                .build();
    }

    @Override
    public PaymentGateway.RefundResult refund(String gatewayReference, BigDecimal amount, String reason) {
        log.info("Flutterwave refund: ref={}, amount={}", gatewayReference, amount);
        return PaymentGateway.RefundResult.builder()
                .success(true)
                .refundReference("refund_" + gatewayReference)
                .build();
    }
}
