package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Create a stock transfer between locations (starts in DRAFT status)")
public class CreateStockTransferRequest {

    @NotBlank
    @Schema(description = "Origin location public ID (where stock is taken from)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
    private String originPublicId;

    @NotBlank
    @Schema(description = "Destination location public ID (where stock will be sent)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private String destinationPublicId;

    @Schema(description = "Expected arrival date", example = "2026-03-20")
    private LocalDate expectedArrivalDate;

    @Schema(description = "Internal note", example = "Monthly restock for branch office")
    private String note;

    @Schema(description = "External reference name (e.g. linked PO or system)", example = "WMS-REF-12345")
    private String referenceName;

    @NotEmpty
    @Valid
    @Schema(description = "Line items to transfer")
    private List<LineItem> lineItems;

    @Data
    @Schema(description = "Stock transfer line item")
    public static class LineItem {

        @NotBlank
        @Schema(description = "Product variant public ID", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ffb7e392-8d86-4f6b-8fdb-abf285885fca")
        private String variantPublicId;

        @Schema(description = "Quantity to transfer", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
        private int quantity;
    }
}
