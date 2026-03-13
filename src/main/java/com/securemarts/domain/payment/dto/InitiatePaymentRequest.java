package com.securemarts.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Initiate payment (redirect to gateway)")
public class InitiatePaymentRequest {

    @NotBlank
    @Schema(description = "Customer email", requiredMode = Schema.RequiredMode.REQUIRED, example = "customer@example.com")
    private String email;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Amount", requiredMode = Schema.RequiredMode.REQUIRED, example = "15000.00")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "NGN")
    private String currency = "NGN";

    @Schema(description = "Order public ID from checkout – include this when paying for an order so the payment is linked", example = "39e7290c-1e40-4424-82fc-af93ebe5ab06")
    private String orderId;

    @Schema(description = "Callback URL after payment", example = "https://mystore.com/payment/callback")
    private String callbackUrl;

    @Schema(description = "Payment gateway", allowableValues = {"PAYSTACK", "FLUTTERWAVE"})
    private String gateway = "PAYSTACK";
}
