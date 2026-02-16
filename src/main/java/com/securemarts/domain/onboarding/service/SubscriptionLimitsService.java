package com.securemarts.domain.onboarding.service;

import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.SubscriptionPlanLimit;
import com.securemarts.domain.onboarding.repository.SubscriptionPlanLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Resolves effective subscription plan and limits for a business.
 * Effective plan = BASIC when trial has ended and there is no active paid subscription.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionLimitsService {

    private final SubscriptionPlanLimitRepository subscriptionPlanLimitRepository;

    /**
     * Effective plan for enforcement: if trial ended and no active subscription, treat as BASIC.
     */
    public Business.SubscriptionPlan getEffectivePlan(Business business) {
        if (business == null) return Business.SubscriptionPlan.BASIC;
        Business.SubscriptionPlan plan = business.getSubscriptionPlan();
        Business.SubscriptionStatus status = business.getSubscriptionStatus();
        Instant trialEndsAt = business.getTrialEndsAt();
        Instant now = Instant.now();

        boolean trialEnded = trialEndsAt != null && now.isAfter(trialEndsAt);
        boolean hasActivePaidSubscription = status == Business.SubscriptionStatus.ACTIVE;

        if (trialEnded && !hasActivePaidSubscription) {
            return Business.SubscriptionPlan.BASIC;
        }
        return plan;
    }

    @Cacheable(value = "subscriptionLimits", key = "#plan.name()", unless = "#result == null")
    public SubscriptionPlanLimit getLimits(Business.SubscriptionPlan plan) {
        return subscriptionPlanLimitRepository.findByPlan(plan.name())
                .orElseThrow(() -> new IllegalStateException("No limits configured for plan: " + plan));
    }

    public SubscriptionPlanLimit getLimitsForBusiness(Business business) {
        return getLimits(getEffectivePlan(business));
    }

    public int getMaxStores(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxStores();
    }

    public int getMaxLocationsPerStore(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxLocationsPerStore();
    }

    public int getMaxProducts(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxProducts();
    }

    public int getMaxStaff(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxStaff();
    }

    public int getMaxPriceRules(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxPriceRules();
    }

    public int getMaxDiscountCodes(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxDiscountCodes();
    }

    public int getMaxPosRegisters(Business.SubscriptionPlan plan) {
        return getLimits(plan).getMaxPosRegisters();
    }

    public boolean isDeliveryEnabled(Business.SubscriptionPlan plan) {
        return getLimits(plan).isDeliveryEnabled();
    }

    public boolean isPosEnabled(Business.SubscriptionPlan plan) {
        return getMaxPosRegisters(plan) > 0;
    }

    /**
     * Paystack plan code for the given interval (monthly or annually).
     */
    public String getPaystackPlanCode(Business.SubscriptionPlan plan, String interval) {
        SubscriptionPlanLimit limits = getLimits(plan);
        if ("annually".equalsIgnoreCase(interval)) {
            return limits.getPaystackPlanCodeAnnual();
        }
        return limits.getPaystackPlanCodeMonthly();
    }
}
