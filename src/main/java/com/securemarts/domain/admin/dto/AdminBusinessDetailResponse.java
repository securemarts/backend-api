package com.securemarts.domain.admin.dto;

import com.securemarts.domain.onboarding.dto.BusinessResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Schema(description = "Admin view of a single business: core info, subscription, counts, and stores")
public class AdminBusinessDetailResponse {

    private String publicId;
    private String legalName;
    private String tradeName;
    private String cacNumber;
    private String logoUrl;
    private String businessTypePublicId;
    private String verificationStatus;
    private Instant createdAt;

    @Schema(description = "Subscription plan: BASIC, PRO, ENTERPRISE")
    private String subscriptionPlan;
    @Schema(description = "Subscription status: NONE, TRIALING, ACTIVE, PAST_DUE, CANCELLED")
    private String subscriptionStatus;
    private Instant trialEndsAt;
    private Instant currentPeriodEndsAt;

    private int storeCount;
    private int ownerCount;
    private int memberCount;
    private long orderCount;

    private List<BusinessResponse.StoreSummary> stores;
}
