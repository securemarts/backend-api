package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.Plan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Plan for list or detail")
public class PlanResponse {

    private String publicId;
    private String name;
    private String code;
    private String description;
    private String billingCycle;
    private String currency;
    private BigDecimal priceAmount;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Schema(description = "Active subscriber count (list only)")
    private Long activeSubscribers;

    @Schema(description = "Plan features (detail only)")
    private List<PlanFeatureItem> features;

    public static PlanResponse from(Plan p) {
        return from(p, null, null);
    }

    public static PlanResponse from(Plan p, Long activeSubscribers, List<PlanFeatureItem> features) {
        return PlanResponse.builder()
                .publicId(p.getPublicId())
                .name(p.getName())
                .code(p.getCode())
                .description(p.getDescription())
                .billingCycle(p.getBillingCycle())
                .currency(p.getCurrency())
                .priceAmount(p.getPriceAmount())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .activeSubscribers(activeSubscribers)
                .features(features != null ? features : (p.getFeatures() != null
                        ? p.getFeatures().stream()
                        .map(f -> new PlanFeatureItem(f.getFeatureKey(), f.isEnabled(), f.getLimitValue()))
                        .collect(Collectors.toList())
                        : List.of()))
                .build();
    }

    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PlanFeatureItem {
        private String featureKey;
        private boolean enabled;
        private Integer limitValue;
    }
}
