package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Add products to a manual collection")
public class AddCollectionProductsRequest {

    @NotEmpty
    @Schema(description = "Product public IDs to add", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> productIds;

    @Schema(description = "Optional position (0-based) to insert at; appends at end if omitted")
    private Integer position;
}
