package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Link a product variant to a location in this store; creates an inventory item with 0 qty if it doesn't exist")
public class CreateInventoryItemRequest {

    @NotBlank
    @Schema(description = "Product variant public ID (from the business catalog)", requiredMode = Schema.RequiredMode.REQUIRED, example = "ffb7e392-8d86-4f6b-8fdb-abf285885fca")
    private String variantPublicId;

    @NotBlank
    @Schema(description = "Location public ID (a place within this store, e.g. warehouse or shop)", requiredMode = Schema.RequiredMode.REQUIRED, example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
    private String locationPublicId;
}
