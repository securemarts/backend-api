package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Current subscription state and limits for a business")
public class SubscriptionResponse {

    @Schema(description = "Effective plan (BASIC when trial ended without payment)")
    private String effectivePlan;

    @Schema(description = "Subscription status", allowableValues = {"NONE", "TRIALING", "ACTIVE", "PAST_DUE", "CANCELLED"})
    private String status;

    private Instant trialEndsAt;
    private Instant currentPeriodEndsAt;

    @Schema(description = "Plan limits")
    private SubscriptionLimitsDto limits;

    @Schema(description = "Current usage")
    private SubscriptionUsageDto usage;

    @Data
    @Builder
    public static class SubscriptionLimitsDto {
        private int maxStores;
        private int maxLocationsPerStore;
        private int maxProducts;
        private int maxStaff;
        private int maxPriceRules;
        private int maxDiscountCodes;
        private int maxPosRegisters;
        private boolean deliveryEnabled;
    }

    @Data
    @Builder
    public static class SubscriptionUsageDto {
        private long storesUsed;
        private long staffUsed;
        private long productsUsed;
        private long locationsUsed;
        private long priceRulesUsed;
        private long discountCodesUsed;
        private long posRegistersUsed;
    }
}
