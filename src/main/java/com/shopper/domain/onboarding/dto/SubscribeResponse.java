package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Redirect user to authorizationUrl to complete subscription payment")
public class SubscribeResponse {

    @Schema(description = "Paystack URL to complete payment and activate subscription")
    private String authorizationUrl;

    @Schema(description = "Transaction reference; pass to POST .../subscription/verify after payment")
    private String reference;
}
