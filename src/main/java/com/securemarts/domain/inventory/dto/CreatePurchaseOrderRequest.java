package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Create a purchase order (starts in DRAFT status)")
public class CreatePurchaseOrderRequest {

    @Schema(description = "Supplier public ID", example = "a3b7c9d1-e2f4-5678-abcd-ef1234567890")
    private String supplierPublicId;

    @NotBlank
    @Schema(description = "Destination location public ID where items will be received",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
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

    @NotEmpty
    @Valid
    @Schema(description = "Line items to order")
    private List<LineItem> lineItems;

    @Data
    @Schema(description = "Purchase order line item")
    public static class LineItem {

        @NotBlank
        @Schema(description = "Product variant public ID", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ffb7e392-8d86-4f6b-8fdb-abf285885fca")
        private String variantPublicId;

        @Schema(description = "Quantity to order", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private int quantity;

        @Schema(description = "Cost price per unit from supplier", example = "1500.00")
        private BigDecimal costPrice;

        @Schema(description = "Retail price per unit (for reference)", example = "2999.00")
        private BigDecimal retailPrice;
    }
}
