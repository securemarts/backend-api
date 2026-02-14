package com.shopper.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Link a product variant to a location in this store; creates an inventory item with 0 qty if it doesn't exist")
public class CreateInventoryItemRequest {

    @NotBlank
    @Schema(description = "Product variant public ID (from the business catalog)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String variantPublicId;

    @NotBlank
    @Schema(description = "Location public ID (a place within this store, e.g. warehouse or shop)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String locationPublicId;
}
