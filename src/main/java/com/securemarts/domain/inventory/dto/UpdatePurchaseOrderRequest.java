package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Update a draft purchase order (only allowed while status is DRAFT)")
public class UpdatePurchaseOrderRequest {

    @Schema(description = "Supplier public ID", example = "a3b7c9d1-e2f4-5678-abcd-ef1234567890")
    private String supplierPublicId;

    @Schema(description = "Destination location public ID", example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
    private String destinationPublicId;

    @Schema(description = "Currency (ISO 4217)", example = "NGN")
    private String currency;

    @Schema(description = "Shipping cost", example = "5000.00")
    private BigDecimal shippingCost;

    @Schema(description = "Adjustments / discounts", example = "0.00")
    private BigDecimal adjustmentsCost;

    @Schema(description = "Tax amount", example = "750.00")
    private BigDecimal taxCost;

    @Schema(description = "Internal note", example = "Rush order for holiday season")
    private String note;

    @Schema(description = "Expected delivery date", example = "2026-03-15")
    private LocalDate expectedOn;

    @Schema(description = "Payment due date", example = "2026-04-15")
    private LocalDate paymentDueOn;

    @Schema(description = "Mark as paid", example = "false")
    private Boolean paid;
}
