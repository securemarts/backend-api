package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.SubscriptionPlanLimit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Public plan summary for landing page (no business-specific data)")
public class SubscriptionPlanPublicResponse {

    private String plan;
    private int maxStores;
    private int maxLocationsPerStore;
    private int maxProducts;
    private int maxStaff;
    private boolean deliveryEnabled;
    private int maxPosRegisters;
    private int maxPriceRules;
    private int maxDiscountCodes;

    public static SubscriptionPlanPublicResponse from(SubscriptionPlanLimit limit) {
        return SubscriptionPlanPublicResponse.builder()
                .plan(limit.getPlan())
                .maxStores(limit.getMaxStores())
                .maxLocationsPerStore(limit.getMaxLocationsPerStore())
                .maxProducts(limit.getMaxProducts())
                .maxStaff(limit.getMaxStaff())
                .deliveryEnabled(limit.isDeliveryEnabled())
                .maxPosRegisters(limit.getMaxPosRegisters())
                .maxPriceRules(limit.getMaxPriceRules())
                .maxDiscountCodes(limit.getMaxDiscountCodes())
                .build();
    }
}
