package com.securemarts.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create order from cart and initiate payment in one call; payment is linked to the order. If delivery fields are set, a delivery order is created on payment success.")
public class CreateOrderAndPayRequest {

    @NotBlank
    @Schema(description = "Cart public ID (from get cart response)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cartId;

    @NotBlank
    @Schema(description = "Customer email for payment", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Callback URL after payment (redirect from gateway)")
    private String callbackUrl;

    @Schema(description = "Payment gateway", example = "PAYSTACK", allowableValues = {"PAYSTACK", "FLUTTERWAVE"})
    private String gateway = "PAYSTACK";

    @Schema(description = "Delivery address (optional). If set with deliveryLat/deliveryLng, delivery order is created when payment succeeds.")
    private String deliveryAddress;

    @Schema(description = "Delivery latitude (required if delivery requested)")
    private BigDecimal deliveryLat;

    @Schema(description = "Delivery longitude (required if delivery requested)")
    private BigDecimal deliveryLng;
}
