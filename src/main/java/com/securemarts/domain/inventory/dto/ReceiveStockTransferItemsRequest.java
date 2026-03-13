package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Receive items at the destination location of a stock transfer.")
public class ReceiveStockTransferItemsRequest {

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

        @Schema(description = "Quantity received", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
        private int quantity;

        @Schema(description = "Quantity rejected", example = "0")
        private int rejectedQuantity;
    }
}
