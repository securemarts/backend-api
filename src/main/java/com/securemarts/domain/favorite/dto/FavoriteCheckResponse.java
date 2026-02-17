package com.securemarts.domain.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response for checking if a product is in the user's favorites")
public class FavoriteCheckResponse {

    @Schema(description = "True if the product is in the user's favorites")
    private boolean favorite;

    @Schema(description = "Favorite record public ID (when favorite is true); use for remove")
    private String favoritePublicId;
}
