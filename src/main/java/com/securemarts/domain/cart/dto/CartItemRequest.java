package com.securemarts.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Add or update cart line")
public class CartItemRequest {

    @NotBlank
    @Schema(description = "Product variant public ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "ffb7e392-8d86-4f6b-8fdb-abf285885fca")
    private String variantPublicId;

    @NotNull
    @Min(1)
    @Schema(description = "Quantity", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer quantity;
}
