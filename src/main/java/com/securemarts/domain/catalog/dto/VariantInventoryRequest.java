package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Inventory quantity at a location for a variant")
public class VariantInventoryRequest {

    @Schema(description = "Location public ID", example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
    private String locationId;

    @Schema(description = "Quantity at this location", example = "50")
    private Integer quantity;
}
