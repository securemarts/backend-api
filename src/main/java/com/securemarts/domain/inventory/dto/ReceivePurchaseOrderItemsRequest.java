package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Receive items against a purchase order. Specify the quantity received (and optionally rejected) per line item.")
public class ReceivePurchaseOrderItemsRequest {

    @NotEmpty
    @Valid
    @Schema(description = "Items being received")
    private List<ReceivedItem> items;

    @Data
    @Schema(description = "Received quantity for a single line item")
    public static class ReceivedItem {

        @NotBlank
        @Schema(description = "Line item public ID", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "c1d2e3f4-a5b6-7890-cdef-123456789abc")
        private String lineItemPublicId;

        @Schema(description = "Quantity received", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        private int quantity;

        @Schema(description = "Quantity rejected (damaged, wrong item, etc.)", example = "0")
        private int rejectedQuantity;
    }
}
