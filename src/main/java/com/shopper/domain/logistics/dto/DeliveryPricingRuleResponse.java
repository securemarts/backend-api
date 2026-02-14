package com.shopper.domain.logistics.dto;

import com.shopper.domain.logistics.entity.DeliveryPricingRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Delivery pricing rule (zone or route based)")
public class DeliveryPricingRuleResponse {

    private String publicId;
    private String zonePublicId;
    private String originHubPublicId;
    private String destinationHubPublicId;
    private BigDecimal baseAmount;
    private BigDecimal perKgAmount;
    private BigDecimal sameCityMultiplier;
    private boolean active;
    private Instant createdAt;

    public static DeliveryPricingRuleResponse from(DeliveryPricingRule rule) {
        return DeliveryPricingRuleResponse.builder()
                .publicId(rule.getPublicId())
                .zonePublicId(rule.getZone() != null ? rule.getZone().getPublicId() : null)
                .originHubPublicId(rule.getOriginHub() != null ? rule.getOriginHub().getPublicId() : null)
                .destinationHubPublicId(rule.getDestinationHub() != null ? rule.getDestinationHub().getPublicId() : null)
                .baseAmount(rule.getBaseAmount())
                .perKgAmount(rule.getPerKgAmount())
                .sameCityMultiplier(rule.getSameCityMultiplier())
                .active(rule.isActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
