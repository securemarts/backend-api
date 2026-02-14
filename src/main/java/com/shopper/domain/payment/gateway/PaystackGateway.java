package com.shopper.domain.payment.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Paystack integration. Docs: https://paystack.com/docs/api/#transaction-initialize
 */
@Component
@Slf4j
public class PaystackGateway implements PaymentGateway {

    private static final String INIT_URL = "https://api.paystack.co/transaction/initialize";

    @Value("${app.payment.paystack.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String name() {
        return "PAYSTACK";
    }

    @Override
    public PaymentGateway.InitResult initiate(PaymentGateway.InitRequest request) {
        log.info("Paystack initiate: ref={}, amount={}", request.getReference(), request.getAmount());
        if (secretKey == null || secretKey.isBlank()) {
            return PaymentGateway.InitResult.builder()
                    .success(false)
                    .message("Paystack not configured")
                    .build();
        }
        try {
            // Amount in kobo (smallest unit): 1 NGN = 100 kobo
            int amountKobo = request.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
            if (amountKobo < 1) {
                return PaymentGateway.InitResult.builder()
                        .success(false)
                        .message("Amount must be at least 0.01 NGN")
                        .build();
            }
            Map<String, Object> body = new HashMap<>();
            body.put("email", request.getEmail());
            body.put("amount", amountKobo);
            body.put("reference", request.getReference());
            body.put("currency", request.getCurrency() != null ? request.getCurrency() : "NGN");
            if (request.getCallbackUrl() != null && !request.getCallbackUrl().isBlank()) {
                body.put("callback_url", request.getCallbackUrl());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + secretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    INIT_URL,
                    HttpMethod.POST,
                    entity,
                    String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean(false);
            JsonNode data = root.path("data");
            String authUrl = data.path("authorization_url").asText(null);
            String ref = data.path("reference").asText(request.getReference());
            String message = root.path("message").asText(null);

            if (!status || authUrl == null || authUrl.isBlank()) {
                return PaymentGateway.InitResult.builder()
                        .success(false)
                        .gatewayReference(ref)
                        .message(message != null ? message : "Paystack initialization failed")
                        .build();
            }
            return PaymentGateway.InitResult.builder()
                    .success(true)
                    .gatewayReference(ref)
                    .authorizationUrl(authUrl)
                    .message(message)
                    .build();
        } catch (Exception e) {
            log.warn("Paystack initiate failed: {}", e.getMessage());
            return PaymentGateway.InitResult.builder()
                    .success(false)
                    .message(e.getMessage() != null ? e.getMessage() : "Paystack request failed")
                    .build();
        }
    }

    @Override
    public PaymentGateway.VerifyResult verify(String gatewayReference) {
        log.info("Paystack verify: {}", gatewayReference);
        if (secretKey == null || secretKey.isBlank()) {
            return PaymentGateway.VerifyResult.builder()
                    .success(false)
                    .gatewayReference(gatewayReference)
                    .message("Paystack not configured")
                    .build();
        }
        try {
            String url = "https://api.paystack.co/transaction/verify/" + gatewayReference;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean(false);
            JsonNode data = root.path("data");
            String trxStatus = data.path("status").asText(""); // success, failed, abandoned, etc.
            String amountStr = data.path("amount").asText("0");
            BigDecimal amount = BigDecimal.valueOf(Long.parseLong(amountStr)).divide(BigDecimal.valueOf(100)); // kobo -> NGN
            String message = root.path("message").asText(null);
            return PaymentGateway.VerifyResult.builder()
                    .success(status && "success".equalsIgnoreCase(trxStatus))
                    .gatewayReference(gatewayReference)
                    .status(trxStatus)
                    .amount(amount)
                    .message(message)
                    .build();
        } catch (Exception e) {
            log.warn("Paystack verify failed: {}", e.getMessage());
            return PaymentGateway.VerifyResult.builder()
                    .success(false)
                    .gatewayReference(gatewayReference)
                    .message(e.getMessage() != null ? e.getMessage() : "Verification failed")
                    .build();
        }
    }

    @Override
    public PaymentGateway.RefundResult refund(String gatewayReference, BigDecimal amount, String reason) {
        log.info("Paystack refund: ref={}, amount={}", gatewayReference, amount);
        return PaymentGateway.RefundResult.builder()
                .success(true)
                .refundReference("refund_" + gatewayReference)
                .build();
    }
}
