package com.securemarts.domain.discovery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "Inventory availability by location")
public class LocationAvailabilityResponse {

    private String locationPublicId;
    private String locationName;
    private String storePublicId;
    @Schema(description = "Variants with quantity and product/variant details for UI")
    private List<VariantAvailabilityDto> variants;

    @Data
    @Builder
    @Schema(description = "Variant availability with product details for buyers")
    public static class VariantAvailabilityDto {
        private String variantPublicId;
        private int quantityAvailable;
        private String productTitle;
        private String variantTitle;
        private String sku;
        private BigDecimal priceAmount;
        private String currency;
        private String imageUrl;
    }
}
