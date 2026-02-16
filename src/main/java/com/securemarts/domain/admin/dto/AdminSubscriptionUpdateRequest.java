package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin update of business subscription (plan, status, trial end). All fields optional.")
public class AdminSubscriptionUpdateRequest {

    @Schema(description = "Subscription plan", allowableValues = {"BASIC", "PRO", "ENTERPRISE"})
    private String plan;

    @Schema(description = "Subscription status", allowableValues = {"NONE", "TRIALING", "ACTIVE", "PAST_DUE", "CANCELLED"})
    private String status;

    @Schema(description = "Trial end timestamp (ISO-8601). Set to extend or clear trial.")
    private Instant trialEndsAt;

    @Schema(description = "Current period end (ISO-8601). For manual/support-led subscription.")
    private Instant currentPeriodEndsAt;
}
