package com.securemarts.domain.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to add a product to favorites")
public class AddFavoriteRequest {

    @NotBlank
    @Schema(description = "Store public ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storePublicId;

    @NotBlank
    @Schema(description = "Product public ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productPublicId;
}
