package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Verify subscription payment with Paystack transaction reference (e.g. after redirect)")
public class VerifySubscriptionRequest {

    @NotBlank
    @Schema(description = "Paystack transaction reference from redirect/callback", requiredMode = Schema.RequiredMode.REQUIRED, example = "sub_abc123")
    private String reference;
}
