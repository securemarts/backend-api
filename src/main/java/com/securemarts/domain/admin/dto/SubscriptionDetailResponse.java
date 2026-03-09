package com.securemarts.domain.admin.dto;

import com.securemarts.domain.onboarding.dto.PlanResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@Schema(description = "Subscription detail for admin (Overview + Features)")
public class SubscriptionDetailResponse {

    @Schema(description = "Business public ID")
    private String businessPublicId;

    @Schema(description = "Merchant display name")
    private String merchantName;

    @Schema(description = "Plan code")
    private String plan;

    @Schema(description = "Plan display name")
    private String planName;

    @Schema(description = "Billing cycle")
    private String billingCycle;

    @Schema(description = "Price from plan")
    private BigDecimal price;

    @Schema(description = "Currency")
    private String currency;

    @Schema(description = "Subscription status")
    private String status;

    @Schema(description = "Start date")
    private Instant startDate;

    @Schema(description = "Renewal date")
    private Instant renewalDate;

    @Schema(description = "Plan features (for Features tab)")
    private List<PlanResponse.PlanFeatureItem> features;
}
