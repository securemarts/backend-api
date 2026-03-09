package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "Create or update product. Include a 'variants' array to set SKU, title, price, etc. You can also add variants later via POST .../products/{productPublicId}/variants.")
public class ProductRequest {

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Product title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 255)
    @Schema(description = "URL handle (auto-generated if blank)")
    private String handle;

    @Schema(description = "HTML description")
    private String bodyHtml;

    @Schema(description = "Product status", allowableValues = {"DRAFT", "ACTIVE", "ARCHIVED"}, example = "DRAFT")
    private String status = "DRAFT";

    @Size(max = 70)
    @Schema(description = "SEO title")
    private String seoTitle;

    @Size(max = 320)
    @Schema(description = "SEO description")
    private String seoDescription;

    @Schema(description = "Collection public ID")
    private String collectionId;

    @Schema(description = "Tag names to associate")
    private Set<String> tagNames;

    @Schema(description = "Variants (SKU, title, price per variant). Optional on create; if omitted, a single default variant is created. Can also add via POST .../products/{id}/variants.")
    private List<ProductVariantRequest> variants;

    @Schema(description = "Media URLs")
    private List<ProductMediaRequest> media;

    @Data
    @Schema(description = "Product variant (size, color, etc.). Used in create/update product body and in POST .../variants.")
    public static class ProductVariantRequest {
        @Size(max = 100)
        @Schema(example = "SHIRT-M-BLK")
        private String sku;
        @Size(max = 255)
        @Schema(example = "Medium / Black")
        private String title;
        @NotNull(message = "priceAmount is required for variants")
        @Schema(description = "Price amount", requiredMode = Schema.RequiredMode.REQUIRED, example = "2999.00")
        private java.math.BigDecimal priceAmount;
        @Schema(description = "Compare-at (original) price for strikethrough", example = "3999.00")
        private java.math.BigDecimal compareAtAmount;
        @Size(max = 3)
        @Schema(example = "NGN")
        private String currency = "NGN";
        @Schema(description = "Optional JSON for custom attributes, e.g. {\"Size\":\"M\",\"Color\":\"Black\"}")
        private String attributesJson;
        @Schema(description = "Display order (0-based)")
        private int position;
    }

    @Data
    @Schema(description = "Product media")
    public static class ProductMediaRequest {
        @NotBlank
        @Size(max = 500)
        private String url;
        @Size(max = 500)
        private String alt;
        private int position;
        @Size(max = 20)
        private String mediaType = "image";
    }
}
