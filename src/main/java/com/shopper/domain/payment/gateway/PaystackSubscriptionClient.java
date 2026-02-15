package com.shopper.domain.payment.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Paystack subscription API: create subscription and initialize first-time payment.
 * Docs: https://paystack.com/docs/api/#subscription-create
 */
@Component
@Slf4j
public class PaystackSubscriptionClient {

    private static final String SUBSCRIPTION_URL = "https://api.paystack.co/subscription";
    private static final String INIT_URL = "https://api.paystack.co/transaction/initialize";
    private static final String VERIFY_URL = "https://api.paystack.co/transaction/verify/";

    @Value("${app.payment.paystack.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a subscription. If customer has no saved card, pass authorizationCode from a successful charge (e.g. webhook).
     */
    public CreateSubscriptionResult createSubscription(String customerEmailOrCode, String planCode,
                                                       Map<String, Object> metadata, String authorizationCode) {
        if (secretKey == null || secretKey.isBlank()) {
            return CreateSubscriptionResult.builder()
                    .success(false)
                    .message("Paystack not configured")
                    .build();
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("customer", customerEmailOrCode);
            body.put("plan", planCode);
            if (authorizationCode != null && !authorizationCode.isBlank()) {
                body.put("authorization", authorizationCode);
            }
            if (metadata != null && !metadata.isEmpty()) {
                body.put("metadata", metadata);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + secretKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(SUBSCRIPTION_URL, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean(false);
            String message = root.path("message").asText(null);
            JsonNode data = root.path("data");

            if (!status) {
                return CreateSubscriptionResult.builder()
                        .success(false)
                        .message(message != null ? message : "Subscription create failed")
                        .build();
            }
            String subscriptionCode = data.path("subscription_code").asText(null);
            String customerCode = data.has("customer") ? data.get("customer").path("customer_code").asText(null) : null;
            if (customerCode == null && data.has("customer")) {
                JsonNode cust = data.get("customer");
                if (cust.isNumber()) customerCode = String.valueOf(cust.asLong());
                else customerCode = cust.path("customer_code").asText(null);
            }
            return CreateSubscriptionResult.builder()
                    .success(true)
                    .subscriptionCode(subscriptionCode)
                    .customerCode(customerCode)
                    .message(message)
                    .build();
        } catch (Exception e) {
            log.warn("Paystack create subscription failed: {}", e.getMessage());
            return CreateSubscriptionResult.builder()
                    .success(false)
                    .message(e.getMessage() != null ? e.getMessage() : "Paystack request failed")
                    .build();
        }
    }

    /**
     * Initialize a one-time transaction (e.g. first subscription payment). Returns URL for customer to complete payment.
     */
    public InitSubscriptionPaymentResult initializeSubscriptionPayment(
            int amountKobo, String email, String callbackUrl, Map<String, Object> metadata) {
        if (secretKey == null || secretKey.isBlank()) {
            return InitSubscriptionPaymentResult.builder()
                    .success(false)
                    .message("Paystack not configured")
                    .build();
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("amount", amountKobo);
            body.put("currency", "NGN");
            body.put("reference", "sub_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20));
            if (callbackUrl != null && !callbackUrl.isBlank()) body.put("callback_url", callbackUrl);
            if (metadata != null && !metadata.isEmpty()) body.put("metadata", metadata);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + secretKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(INIT_URL, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean(false);
            JsonNode data = root.path("data");
            String authUrl = data.path("authorization_url").asText(null);
            String ref = data.path("reference").asText(null);
            String message = root.path("message").asText(null);

            if (!status || authUrl == null || authUrl.isBlank()) {
                return InitSubscriptionPaymentResult.builder()
                        .success(false)
                        .reference(ref)
                        .message(message != null ? message : "Initialize failed")
                        .build();
            }
            return InitSubscriptionPaymentResult.builder()
                    .success(true)
                    .authorizationUrl(authUrl)
                    .reference(ref)
                    .message(message)
                    .build();
        } catch (Exception e) {
            log.warn("Paystack init subscription payment failed: {}", e.getMessage());
            return InitSubscriptionPaymentResult.builder()
                    .success(false)
                    .message(e.getMessage() != null ? e.getMessage() : "Paystack request failed")
                    .build();
        }
    }

    /**
     * Verify a transaction by reference (e.g. after redirect from Paystack). Returns metadata and authorization for subscription activation.
     */
    public VerifyTransactionResult verifyTransaction(String reference) {
        if (secretKey == null || secretKey.isBlank()) {
            return VerifyTransactionResult.builder()
                    .success(false)
                    .message("Paystack not configured")
                    .build();
        }
        if (reference == null || reference.isBlank()) {
            return VerifyTransactionResult.builder()
                    .success(false)
                    .message("Reference is required")
                    .build();
        }
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(VERIFY_URL + reference, org.springframework.http.HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean status = root.path("status").asBoolean(false);
            JsonNode data = root.path("data");
            String trxStatus = data.path("status").asText("");
            if (!status || !"success".equalsIgnoreCase(trxStatus)) {
                return VerifyTransactionResult.builder()
                        .success(false)
                        .message(root.path("message").asText("Verification failed"))
                        .build();
            }
            JsonNode metadata = data.path("metadata");
            String businessPublicId = metadata.path("business_public_id").asText(null);
            String plan = metadata.path("plan").asText(null);
            String interval = metadata.path("interval").asText("monthly");
            String email = data.path("customer").path("email").asText(null);
            if ((email == null || email.isBlank()) && data.has("authorization")) {
                email = data.path("authorization").path("email").asText(null);
            }
            JsonNode auth = data.path("authorization");
            String authorizationCode = auth.path("authorization_code").asText(null);
            return VerifyTransactionResult.builder()
                    .success(true)
                    .businessPublicId(businessPublicId)
                    .plan(plan)
                    .interval(interval)
                    .authorizationCode(authorizationCode)
                    .email(email)
                    .build();
        } catch (Exception e) {
            log.warn("Paystack verify transaction failed: {}", e.getMessage());
            return VerifyTransactionResult.builder()
                    .success(false)
                    .message(e.getMessage() != null ? e.getMessage() : "Verification failed")
                    .build();
        }
    }

    @Data
    @Builder
    public static class CreateSubscriptionResult {
        private boolean success;
        private String subscriptionCode;
        private String customerCode;
        private String message;
    }

    @Data
    @Builder
    public static class InitSubscriptionPaymentResult {
        private boolean success;
        private String authorizationUrl;
        private String reference;
        private String message;
    }

    @Data
    @Builder
    public static class VerifyTransactionResult {
        private boolean success;
        private String message;
        private String businessPublicId;
        private String plan;
        private String interval;
        private String authorizationCode;
        private String email;
    }
}
