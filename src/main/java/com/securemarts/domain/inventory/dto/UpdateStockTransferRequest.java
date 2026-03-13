package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Update a draft stock transfer (only allowed while status is DRAFT)")
public class UpdateStockTransferRequest {

    @Schema(description = "Origin location public ID", example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
    private String originPublicId;

    @Schema(description = "Destination location public ID", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private String destinationPublicId;

    @Schema(description = "Expected arrival date", example = "2026-03-20")
    private LocalDate expectedArrivalDate;

    @Schema(description = "Internal note", example = "Monthly restock for branch office")
    private String note;

    @Schema(description = "External reference name", example = "WMS-REF-12345")
    private String referenceName;
}
