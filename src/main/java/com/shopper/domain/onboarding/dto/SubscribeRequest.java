package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Start a Paystack subscription for Pro or Enterprise")
public class SubscribeRequest {

    @NotBlank
    @Schema(description = "Plan to subscribe to", allowableValues = {"PRO", "ENTERPRISE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String plan;

    @Schema(description = "Billing interval", allowableValues = {"monthly", "annually"}, example = "monthly")
    private String interval = "monthly";

    @Schema(description = "Callback URL after payment (redirect from Paystack)")
    private String callbackUrl;
}
