package com.securemarts.domain.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Schema(description = "Request to record a payment against an invoice")
public class RecordPaymentRequest {

    @NotNull
    @DecimalMin("0.0001")
    @Schema(description = "Payment amount", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Payment method", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"CASH", "BANK_TRANSFER", "POS", "OTHER"})
    private String paymentMethod;

    @Schema(description = "When the payment was made (default: now)")
    private Instant paidAt;

    @Schema(description = "Optional reference (e.g. bank ref)")
    private String reference;
}
