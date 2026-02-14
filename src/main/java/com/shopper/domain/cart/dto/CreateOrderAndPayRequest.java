package com.shopper.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Create order from cart and initiate payment in one call; payment is linked to the order")
public class CreateOrderAndPayRequest {

    @NotBlank
    @Schema(description = "Cart public ID (from get cart response)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cartId;

    @NotBlank
    @Schema(description = "Customer email for payment", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Callback URL after payment (redirect from gateway)")
    private String callbackUrl;

    @Schema(description = "Gateway: PAYSTACK or FLUTTERWAVE", example = "PAYSTACK")
    private String gateway = "PAYSTACK";
}
