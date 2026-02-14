package com.shopper.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "Create or update product")
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

    @Schema(description = "Status", example = "ACTIVE")
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

    @Schema(description = "Variants")
    private List<ProductVariantRequest> variants;

    @Schema(description = "Media URLs")
    private List<ProductMediaRequest> media;

    @Data
    @Schema(description = "Product variant")
    public static class ProductVariantRequest {
        @Size(max = 100)
        private String sku;
        @Size(max = 255)
        private String title;
        @Schema(description = "Price amount", required = true)
        private java.math.BigDecimal priceAmount;
        private java.math.BigDecimal compareAtAmount;
        @Size(max = 3)
        private String currency = "NGN";
        private String attributesJson;
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
