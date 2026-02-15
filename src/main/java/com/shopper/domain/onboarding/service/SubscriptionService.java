package com.shopper.domain.onboarding.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.auth.repository.UserRepository;
import com.shopper.domain.onboarding.dto.SubscriptionResponse;
import com.shopper.domain.onboarding.dto.SubscribeRequest;
import com.shopper.domain.onboarding.dto.SubscribeResponse;
import com.shopper.domain.onboarding.dto.VerifySubscriptionRequest;
import com.shopper.domain.onboarding.entity.Business;
import com.shopper.domain.onboarding.entity.SubscriptionHistory;
import com.shopper.domain.onboarding.entity.SubscriptionPlanLimit;
import com.shopper.domain.onboarding.repository.*;
import com.shopper.domain.catalog.repository.ProductRepository;
import com.shopper.domain.inventory.repository.LocationRepository;
import com.shopper.domain.payment.gateway.PaystackSubscriptionClient;
import com.shopper.domain.pos.repository.POSRegisterRepository;
import com.shopper.domain.pricing.repository.DiscountCodeRepository;
import com.shopper.domain.pricing.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final POSRegisterRepository posRegisterRepository;
    private final PaystackSubscriptionClient paystackSubscriptionClient;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Value("${app.subscription.paystack.pro-monthly-amount-kobo:2000000}")
    private int proMonthlyAmountKobo;
    @Value("${app.subscription.paystack.pro-annual-amount-kobo:20000000}")
    private int proAnnualAmountKobo;
    @Value("${app.subscription.paystack.enterprise-monthly-amount-kobo:5000000}")
    private int enterpriseMonthlyAmountKobo;
    @Value("${app.subscription.paystack.enterprise-annual-amount-kobo:50000000}")
    private int enterpriseAnnualAmountKobo;

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(String userPublicId, String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        var effectivePlan = subscriptionLimitsService.getEffectivePlan(business);
        SubscriptionPlanLimit limits = subscriptionLimitsService.getLimits(effectivePlan);

        long storesUsed = storeRepository.countByBusinessId(business.getId());
        long staffUsed = businessMemberRepository.countByBusinessId(business.getId());
        long productsUsed = 0;
        long locationsUsed = 0;
        long priceRulesUsed = 0;
        long discountCodesUsed = 0;
        long posRegistersUsed = 0;
        List<Long> storeIds = storeRepository.findByBusinessId(business.getId()).stream()
                .map(s -> s.getId()).toList();
        for (Long storeId : storeIds) {
            productsUsed += productRepository.countByStoreIdAndDeletedAtIsNull(storeId);
            locationsUsed += locationRepository.countByStoreId(storeId);
            priceRulesUsed += priceRuleRepository.countByStoreId(storeId);
            discountCodesUsed += discountCodeRepository.countByStoreId(storeId);
            posRegistersUsed += posRegisterRepository.countByStoreId(storeId);
        }

        return SubscriptionResponse.builder()
                .effectivePlan(effectivePlan.name())
                .status(business.getSubscriptionStatus().name())
                .trialEndsAt(business.getTrialEndsAt())
                .currentPeriodEndsAt(business.getCurrentPeriodEndsAt())
                .limits(SubscriptionResponse.SubscriptionLimitsDto.builder()
                        .maxStores(limits.getMaxStores())
                        .maxLocationsPerStore(limits.getMaxLocationsPerStore())
                        .maxProducts(limits.getMaxProducts())
                        .maxStaff(limits.getMaxStaff())
                        .maxPriceRules(limits.getMaxPriceRules())
                        .maxDiscountCodes(limits.getMaxDiscountCodes())
                        .maxPosRegisters(limits.getMaxPosRegisters())
                        .deliveryEnabled(limits.isDeliveryEnabled())
                        .build())
                .usage(SubscriptionResponse.SubscriptionUsageDto.builder()
                        .storesUsed(storesUsed)
                        .staffUsed(staffUsed)
                        .productsUsed(productsUsed)
                        .locationsUsed(locationsUsed)
                        .priceRulesUsed(priceRulesUsed)
                        .discountCodesUsed(discountCodesUsed)
                        .posRegistersUsed(posRegistersUsed)
                        .build())
                .build();
    }

    /**
     * Initiates Paystack subscription; returns authorization URL for the customer to complete payment (or null if subscription was created immediately).
     */
    @Transactional
    public SubscribeResponse subscribe(String userPublicId, String businessPublicId, SubscribeRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        Business.SubscriptionPlan requestedPlan = "ENTERPRISE".equals(request.getPlan())
                ? Business.SubscriptionPlan.ENTERPRISE
                : Business.SubscriptionPlan.PRO;
        if (!"PRO".equals(request.getPlan()) && !"ENTERPRISE".equals(request.getPlan())) {
            throw new IllegalArgumentException("Plan must be PRO or ENTERPRISE");
        }
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException("User email is required for subscription");
        }
        String interval = request.getInterval() != null ? request.getInterval() : "monthly";
        String planCode = subscriptionLimitsService.getPaystackPlanCode(requestedPlan, interval);
        Map<String, Object> metadata = Map.of("business_public_id", businessPublicId, "plan", request.getPlan(), "interval", interval);

        // Try create subscription (succeeds if customer has existing Paystack authorization)
        if (planCode != null && !planCode.isBlank()) {
            var createResult = paystackSubscriptionClient.createSubscription(email, planCode, metadata, null);
            if (createResult.isSuccess() && createResult.getSubscriptionCode() != null) {
                business.setPaystackSubscriptionCode(createResult.getSubscriptionCode());
                if (createResult.getCustomerCode() != null) business.setPaystackCustomerCode(createResult.getCustomerCode());
                business.setSubscriptionPlan(requestedPlan);
                business.setSubscriptionStatus(Business.SubscriptionStatus.ACTIVE);
                business.setCurrentPeriodEndsAt(interval.toLowerCase().startsWith("annual")
                        ? Instant.now().plus(365, ChronoUnit.DAYS)
                        : Instant.now().plus(30, ChronoUnit.DAYS));
                businessRepository.save(business);
                SubscriptionHistory h = new SubscriptionHistory();
                h.setBusinessId(business.getId());
                h.setPlan(requestedPlan.name());
                h.setEventType(SubscriptionHistory.EventType.ACTIVATED.name());
                h.setPaystackSubscriptionCode(createResult.getSubscriptionCode());
                h.setPeriodEnd(business.getCurrentPeriodEndsAt());
                h.setSource(SubscriptionHistory.Source.APP.name());
                subscriptionHistoryRepository.save(h);
                return SubscribeResponse.builder().authorizationUrl(null).build();
            }
        }

        // First-time or no plan code: initialize one-time payment; webhook will create subscription on success
        int amountKobo = amountKoboForPlanAndInterval(request.getPlan(), interval);
        var initResult = paystackSubscriptionClient.initializeSubscriptionPayment(
                amountKobo, email, request.getCallbackUrl(), metadata);
        if (initResult.isSuccess() && initResult.getAuthorizationUrl() != null) {
            return SubscribeResponse.builder()
                    .authorizationUrl(initResult.getAuthorizationUrl())
                    .reference(initResult.getReference())
                    .build();
        }
        throw new BusinessRuleException(initResult.getMessage() != null ? initResult.getMessage() : "Could not start subscription payment");
    }

    /**
     * Verify subscription payment with Paystack transaction reference (e.g. after user returns from Paystack redirect).
     * Activates the subscription locally when verification succeeds; same outcome as webhook charge.success.
     */
    @Transactional
    public void verifySubscriptionPayment(String userPublicId, String businessPublicId, VerifySubscriptionRequest request) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());

        PaystackSubscriptionClient.VerifyTransactionResult verify = paystackSubscriptionClient.verifyTransaction(request.getReference());
        if (!verify.isSuccess()) {
            throw new BusinessRuleException(verify.getMessage() != null ? verify.getMessage() : "Payment verification failed");
        }
        if (verify.getBusinessPublicId() == null || !verify.getBusinessPublicId().equals(businessPublicId)) {
            throw new BusinessRuleException("Transaction does not belong to this business");
        }
        String plan = verify.getPlan();
        if (plan == null || (!"PRO".equals(plan) && !"ENTERPRISE".equals(plan))) {
            throw new BusinessRuleException("Transaction is not for a subscription plan");
        }
        Business.SubscriptionPlan planEnum = "ENTERPRISE".equals(plan) ? Business.SubscriptionPlan.ENTERPRISE : Business.SubscriptionPlan.PRO;
        String interval = verify.getInterval() != null ? verify.getInterval() : "monthly";
        String planCode = subscriptionLimitsService.getPaystackPlanCode(planEnum, interval);
        String email = verify.getEmail();
        String authorizationCode = verify.getAuthorizationCode();

        if (planCode != null && !planCode.isBlank() && authorizationCode != null && !authorizationCode.isBlank() && email != null && !email.isBlank()) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("business_public_id", businessPublicId);
            meta.put("plan", plan);
            meta.put("interval", interval);
            var createResult = paystackSubscriptionClient.createSubscription(email, planCode, meta, authorizationCode);
            if (createResult.isSuccess() && createResult.getSubscriptionCode() != null) {
                business.setPaystackSubscriptionCode(createResult.getSubscriptionCode());
                if (createResult.getCustomerCode() != null) business.setPaystackCustomerCode(createResult.getCustomerCode());
                business.setSubscriptionPlan(planEnum);
                business.setSubscriptionStatus(Business.SubscriptionStatus.ACTIVE);
                business.setCurrentPeriodEndsAt(interval.toLowerCase().startsWith("annual")
                        ? Instant.now().plus(365, ChronoUnit.DAYS)
                        : Instant.now().plus(30, ChronoUnit.DAYS));
                businessRepository.save(business);
                SubscriptionHistory h = new SubscriptionHistory();
                h.setBusinessId(business.getId());
                h.setPlan(planEnum.name());
                h.setEventType(SubscriptionHistory.EventType.ACTIVATED.name());
                h.setPaystackSubscriptionCode(createResult.getSubscriptionCode());
                h.setPeriodEnd(business.getCurrentPeriodEndsAt());
                h.setSource(SubscriptionHistory.Source.APP.name());
                subscriptionHistoryRepository.save(h);
                return;
            }
        }
        // No Paystack plan code or create failed: activate with period only
        business.setSubscriptionPlan(planEnum);
        business.setSubscriptionStatus(Business.SubscriptionStatus.ACTIVE);
        business.setCurrentPeriodEndsAt(interval.toLowerCase().startsWith("annual")
                ? Instant.now().plus(365, ChronoUnit.DAYS)
                : Instant.now().plus(30, ChronoUnit.DAYS));
        businessRepository.save(business);
        SubscriptionHistory h = new SubscriptionHistory();
        h.setBusinessId(business.getId());
        h.setPlan(planEnum.name());
        h.setEventType(SubscriptionHistory.EventType.ACTIVATED.name());
        h.setPeriodEnd(business.getCurrentPeriodEndsAt());
        h.setSource(SubscriptionHistory.Source.APP.name());
        subscriptionHistoryRepository.save(h);
    }

    /**
     * Start 14-day Pro trial. Only allowed when effective plan is BASIC (no active trial or paid plan).
     */
    @Transactional
    public void startTrial(String userPublicId, String businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessPublicId));
        ensureUserOwnsBusiness(userPublicId, business.getId());
        Business.SubscriptionPlan effectivePlan = subscriptionLimitsService.getEffectivePlan(business);
        if (effectivePlan != Business.SubscriptionPlan.BASIC) {
            throw new BusinessRuleException("Trial is only available for Basic plan. You are already on " + effectivePlan.name() + " or an active trial.");
        }
        if (business.getSubscriptionStatus() == Business.SubscriptionStatus.TRIALING) {
            throw new BusinessRuleException("You already have an active trial.");
        }
        if (subscriptionHistoryRepository.existsByBusinessIdAndEventType(business.getId(), SubscriptionHistory.EventType.TRIAL_STARTED.name())) {
            throw new BusinessRuleException("You have already used your one-time Pro trial.");
        }
        business.setSubscriptionPlan(Business.SubscriptionPlan.PRO);
        business.setSubscriptionStatus(Business.SubscriptionStatus.TRIALING);
        business.setTrialEndsAt(Instant.now().plus(14, ChronoUnit.DAYS));
        businessRepository.save(business);
        SubscriptionHistory history = new SubscriptionHistory();
        history.setBusinessId(business.getId());
        history.setPlan(Business.SubscriptionPlan.PRO.name());
        history.setEventType(SubscriptionHistory.EventType.TRIAL_STARTED.name());
        history.setPeriodEnd(business.getTrialEndsAt());
        history.setSource(SubscriptionHistory.Source.START_TRIAL.name());
        subscriptionHistoryRepository.save(history);
    }

    private int amountKoboForPlanAndInterval(String plan, String interval) {
        boolean annual = interval != null && interval.toLowerCase().startsWith("annual");
        if ("ENTERPRISE".equals(plan)) return annual ? enterpriseAnnualAmountKobo : enterpriseMonthlyAmountKobo;
        return annual ? proAnnualAmountKobo : proMonthlyAmountKobo;
    }

    private void ensureUserOwnsBusiness(String userPublicId, Long businessId) {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        if (businessOwnerRepository.findByBusinessIdAndUserId(businessId, user.getId()).isEmpty()) {
            throw new BusinessRuleException("You do not own this business");
        }
    }
}
