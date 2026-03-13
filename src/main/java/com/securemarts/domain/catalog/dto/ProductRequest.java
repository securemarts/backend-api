package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Schema(description = "Create or update product. Include options, variants (with option map and inventory per location), and optional media.")
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

    @Size(max = 255)
    @Schema(description = "Vendor or brand")
    private String vendor;

    @Size(max = 255)
    @Schema(description = "Product type")
    private String productType;

    @Size(max = 70)
    @Schema(description = "SEO title")
    private String seoTitle;

    @Size(max = 320)
    @Schema(description = "SEO description")
    private String seoDescription;

    @Schema(description = "Collection public IDs (product can be in multiple collections)")
    private List<String> collectionIds;

    @Schema(description = "Tag names to associate")
    private Set<String> tagNames;

    @Schema(description = "Product options (e.g. Size → S,M,L; Color → Black,White)")
    private List<ProductOptionRequest> options;

    @Schema(description = "Variants (SKU, title, price, options map, inventory per location). Optional on create; if omitted, a single default variant is created.")
    private List<ProductVariantRequest> variants;

    @Schema(description = "Media URLs")
    private List<ProductMediaRequest> media;

    @Data
    @Schema(description = "Product variant with option values and inventory per location")
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
        @Schema(description = "Compare-at (original) price", example = "3999.00")
        private java.math.BigDecimal compareAtAmount;
        @Size(max = 3)
        @Schema(example = "NGN")
        private String currency = "NGN";
        @Size(max = 100)
        @Schema(description = "Barcode")
        private String barcode;
        @Schema(description = "Weight")
        private java.math.BigDecimal weight;
        @Size(max = 10)
        @Schema(description = "Weight unit", example = "kg")
        private String weightUnit;
        @Schema(description = "Track inventory for this variant", example = "true")
        private boolean trackInventory = true;
        @Schema(description = "Requires shipping", example = "true")
        private boolean requiresShipping = true;
        @Schema(description = "Option name → value, e.g. {\"Size\":\"M\",\"Color\":\"Black\"}")
        private Map<String, String> options;
        @Schema(description = "Inventory quantities per location (locationId → quantity)")
        private List<VariantInventoryRequest> inventory;
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
