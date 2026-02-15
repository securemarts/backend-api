package com.shopper.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopper.domain.onboarding.entity.Business;
import com.shopper.domain.onboarding.entity.SubscriptionHistory;
import com.shopper.domain.onboarding.repository.BusinessRepository;
import com.shopper.domain.onboarding.repository.SubscriptionHistoryRepository;
import com.shopper.domain.onboarding.service.SubscriptionLimitsService;
import com.shopper.domain.payment.gateway.PaystackSubscriptionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.HexFormat;

/**
 * Verifies Paystack webhook signature and processes subscription-related events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackWebhookService {

    @Value("${app.payment.paystack.secret-key:}")
    private String secretKey;

    private final BusinessRepository businessRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PaystackSubscriptionClient paystackSubscriptionClient;
    private final SubscriptionLimitsService subscriptionLimitsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Verify x-paystack-signature: HMAC SHA512 of request body with secret key.
     */
    public boolean verifySignature(String rawBody, String signature) {
        if (secretKey == null || secretKey.isBlank() || signature == null || signature.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn("Paystack signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Process webhook payload. Returns true if event was handled.
     */
    @Transactional
    public boolean handleEvent(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText(null);
            JsonNode data = root.path("data");
            if (event == null || event.isBlank()) return false;

            switch (event) {
                case "charge.success" -> handleChargeSuccess(data);
                case "subscription.create", "subscription.enable" -> handleSubscriptionCreate(data);
                case "subscription.disable" -> handleSubscriptionDisable(data);
                case "invoice.payment_failed" -> handleInvoicePaymentFailed(data);
                default -> log.debug("Paystack webhook event ignored: {}", event);
            }
            return true;
        } catch (JsonProcessingException e) {
            log.warn("Paystack webhook invalid JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid webhook payload", e);
        } catch (Exception e) {
            log.warn("Paystack webhook handle failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void handleChargeSuccess(JsonNode data) {
        JsonNode metadata = data.path("metadata");
        String businessPublicId = metadata.path("business_public_id").asText(null);
        if (businessPublicId == null || businessPublicId.isBlank()) return;

        String plan = metadata.path("plan").asText(null);
        String interval = metadata.path("interval").asText("monthly");
        if (plan == null || (!"PRO".equals(plan) && !"ENTERPRISE".equals(plan))) return;

        Business.SubscriptionPlan planEnum = "ENTERPRISE".equals(plan) ? Business.SubscriptionPlan.ENTERPRISE : Business.SubscriptionPlan.PRO;
        String planCode = subscriptionLimitsService.getPaystackPlanCode(planEnum, interval);
        if (planCode == null || planCode.isBlank()) {
            // No Paystack plan code: still activate subscription with period
            Business business = businessRepository.findByPublicId(businessPublicId).orElse(null);
            if (business != null) {
                business.setSubscriptionPlan(planEnum);
                business.setSubscriptionStatus(Business.SubscriptionStatus.ACTIVE);
                business.setCurrentPeriodEndsAt(interval.toLowerCase().startsWith("annual")
                        ? Instant.now().plus(365, ChronoUnit.DAYS)
                        : Instant.now().plus(30, ChronoUnit.DAYS));
                businessRepository.save(business);
                saveHistory(business.getId(), planEnum.name(), SubscriptionHistory.EventType.ACTIVATED, null, null, business.getCurrentPeriodEndsAt());
            }
            return;
        }

        String email = data.path("customer").path("email").asText(null);
        if (email == null || email.isBlank()) return;
        JsonNode auth = data.path("authorization");
        String authorizationCode = auth.path("authorization_code").asText(null);
        if (authorizationCode == null || authorizationCode.isBlank()) return;

        Map<String, Object> meta = new HashMap<>();
        meta.put("business_public_id", businessPublicId);
        meta.put("plan", plan);
        meta.put("interval", interval);
        var result = paystackSubscriptionClient.createSubscription(email, planCode, meta, authorizationCode);
        if (result.isSuccess() && result.getSubscriptionCode() != null) {
            Business business = businessRepository.findByPublicId(businessPublicId).orElse(null);
            if (business != null) {
                business.setPaystackSubscriptionCode(result.getSubscriptionCode());
                if (result.getCustomerCode() != null) business.setPaystackCustomerCode(result.getCustomerCode());
                business.setSubscriptionPlan(planEnum);
                business.setSubscriptionStatus(Business.SubscriptionStatus.ACTIVE);
                business.setCurrentPeriodEndsAt(interval.toLowerCase().startsWith("annual")
                        ? Instant.now().plus(365, ChronoUnit.DAYS)
                        : Instant.now().plus(30, ChronoUnit.DAYS));
                businessRepository.save(business);
                saveHistory(business.getId(), planEnum.name(), SubscriptionHistory.EventType.ACTIVATED, result.getSubscriptionCode(), null, business.getCurrentPeriodEndsAt());
            }
        }
    }

    private void handleSubscriptionCreate(JsonNode data) {
        String subscriptionCode = data.path("subscription_code").asText(null);
        if (subscriptionCode == null || subscriptionCode.isBlank()) return;
        JsonNode metadata = data.path("metadata");
        String businessPublicId = metadata.path("business_public_id").asText(null);
        if (businessPublicId == null || businessPublicId.isBlank()) return;

        Business business = businessRepository.findByPublicId(businessPublicId).orElse(null);
        if (business == null) return;
        business.setPaystackSubscriptionCode(subscriptionCode);
        JsonNode customer = data.path("customer");
        if (!customer.isMissingNode()) {
            String customerCode = customer.path("customer_code").asText(null);
            if (customerCode == null && customer.isNumber()) customerCode = String.valueOf(customer.asLong());
            if (customerCode != null) business.setPaystackCustomerCode(customerCode);
        }
        business.setSubscriptionStatus(Business.SubscriptionStatus.ACTIVE);
        String nextPayment = data.path("next_payment_date").asText(null);
        if (nextPayment != null && !nextPayment.isBlank()) {
            try {
                business.setCurrentPeriodEndsAt(Instant.parse(nextPayment));
            } catch (Exception ignored) {}
        }
        if (business.getCurrentPeriodEndsAt() == null) {
            business.setCurrentPeriodEndsAt(Instant.now().plus(30, ChronoUnit.DAYS));
        }
        businessRepository.save(business);
        saveHistory(business.getId(), business.getSubscriptionPlan().name(), SubscriptionHistory.EventType.ACTIVATED, subscriptionCode, null, business.getCurrentPeriodEndsAt());
    }

    private void handleSubscriptionDisable(JsonNode data) {
        String subscriptionCode = data.path("subscription_code").asText(null);
        if (subscriptionCode == null || subscriptionCode.isBlank()) return;
        businessRepository.findAll().stream()
                .filter(b -> subscriptionCode.equals(b.getPaystackSubscriptionCode()))
                .findFirst()
                .ifPresent(b -> {
                    b.setSubscriptionStatus(Business.SubscriptionStatus.CANCELLED);
                    businessRepository.save(b);
                    saveHistory(b.getId(), b.getSubscriptionPlan().name(), SubscriptionHistory.EventType.CANCELLED, subscriptionCode, null, null);
                });
    }

    private void handleInvoicePaymentFailed(JsonNode data) {
        JsonNode subscription = data.path("subscription");
        if (subscription.isMissingNode()) return;
        String subscriptionCode = subscription.path("subscription_code").asText(null);
        if (subscriptionCode == null || subscriptionCode.isBlank()) return;
        businessRepository.findAll().stream()
                .filter(b -> subscriptionCode.equals(b.getPaystackSubscriptionCode()))
                .findFirst()
                .ifPresent(b -> {
                    b.setSubscriptionStatus(Business.SubscriptionStatus.PAST_DUE);
                    businessRepository.save(b);
                    saveHistory(b.getId(), b.getSubscriptionPlan().name(), SubscriptionHistory.EventType.PAST_DUE, subscriptionCode, null, null);
                });
    }

    private void saveHistory(Long businessId, String plan, SubscriptionHistory.EventType eventType, String paystackCode, Instant periodStart, Instant periodEnd) {
        SubscriptionHistory h = new SubscriptionHistory();
        h.setBusinessId(businessId);
        h.setPlan(plan);
        h.setEventType(eventType.name());
        h.setPaystackSubscriptionCode(paystackCode);
        h.setPeriodStart(periodStart);
        h.setPeriodEnd(periodEnd);
        h.setSource(SubscriptionHistory.Source.WEBHOOK.name());
        subscriptionHistoryRepository.save(h);
    }
}
