package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Subscription row for admin list")
public class SubscriptionListResponse {

    @Schema(description = "Business (merchant) public ID")
    private String businessPublicId;

    @Schema(description = "Merchant display name (trade name or first store name)")
    private String merchantName;

    @Schema(description = "Plan code (e.g. BASIC, PRO)")
    private String plan;

    @Schema(description = "Plan display name")
    private String planName;

    @Schema(description = "Billing cycle from plan")
    private String billingCycle;

    @Schema(description = "Subscription start date (earliest period or first event)")
    private Instant startDate;

    @Schema(description = "Renewal / current period end")
    private Instant renewalDate;

    @Schema(description = "Subscription status")
    private String status;
}
