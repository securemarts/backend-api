package com.shopper.domain.storefront.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Public store info for storefront (active stores only)")
public class StorefrontStoreDto {

    @Schema(description = "Store public ID; use for cart and checkout APIs")
    private String publicId;

    private String name;

    @Schema(description = "URL-friendly slug (e.g. acme-store)")
    private String domainSlug;

    @Schema(description = "Default currency code (e.g. NGN)")
    private String defaultCurrency;
}
