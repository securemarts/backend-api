package com.shopper.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Adjust inventory quantity (positive = add, negative = deduct)")
public class InventoryAdjustmentRequest {

    @NotNull
    @Schema(description = "Quantity delta (positive for restock, negative for deduction)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantityDelta;

    @Schema(description = "Inventory movement type", allowableValues = {"ADJUSTMENT", "SALE", "RESTOCK", "RESERVE", "RELEASE", "RETURN", "TRANSFER_IN", "TRANSFER_OUT"}, example = "ADJUSTMENT")
    private String movementType = "ADJUSTMENT";

    @Schema(description = "Reference type (e.g. ORDER, RETURN)")
    private String referenceType;

    @Schema(description = "Reference ID")
    private String referenceId;
}
