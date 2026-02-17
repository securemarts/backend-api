package com.securemarts.domain.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "A favorite (wishlist) item with store and product summary")
public class FavoriteResponse {

    @Schema(description = "Favorite record public ID (use for remove)")
    private String publicId;

    @Schema(description = "Store public ID")
    private String storePublicId;

    @Schema(description = "Store name")
    private String storeName;

    @Schema(description = "Store domain slug (for storefront URL)")
    private String storeSlug;

    @Schema(description = "Product public ID")
    private String productPublicId;

    @Schema(description = "Product title")
    private String productTitle;

    @Schema(description = "First product image URL (if any)")
    private String productImageUrl;

    @Schema(description = "Product price from first variant (if any)")
    private BigDecimal productPrice;

    @Schema(description = "Currency code (e.g. NGN)")
    private String productCurrency;

    @Schema(description = "When the item was added to favorites")
    private Instant createdAt;
}
