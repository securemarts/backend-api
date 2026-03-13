package com.securemarts.domain.catalog.dto;

import com.securemarts.domain.catalog.entity.Product;
import com.securemarts.domain.catalog.entity.ProductVariant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Product response")
public class ProductResponse {

    private String publicId;
    private String title;
    private String handle;
    private String bodyHtml;
    @Schema(description = "Product status", allowableValues = {"DRAFT", "ACTIVE", "ARCHIVED"})
    private String status;
    private String vendor;
    private String productType;
    private String seoTitle;
    private String seoDescription;
    @Schema(description = "When the product was published")
    private Instant publishedAt;
    @Schema(description = "Collection public IDs (product can be in multiple collections)")
    private List<String> collectionIds;
    @Schema(description = "Store ID; products belong to a store")
    private Long storeId;
    private List<String> tagNames;
    private List<OptionResponse> options;
    private List<VariantResponse> variants;
    private List<MediaResponse> media;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .publicId(p.getPublicId())
                .title(p.getTitle())
                .handle(p.getHandle())
                .bodyHtml(p.getBodyHtml())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .vendor(p.getVendor())
                .productType(p.getProductType())
                .seoTitle(p.getSeoTitle())
                .seoDescription(p.getSeoDescription())
                .publishedAt(p.getPublishedAt())
                .collectionIds(p.getCollectionProducts() != null ? p.getCollectionProducts().stream()
                        .map(cp -> cp.getCollection() != null ? cp.getCollection().getPublicId() : null)
                        .filter(Objects::nonNull).distinct().toList() : List.of())
                .storeId(p.getStoreId())
                .tagNames(p.getTags() != null ? p.getTags().stream().map(com.securemarts.domain.catalog.entity.Tag::getName).collect(Collectors.toList()) : List.of())
                .options(p.getOptions() != null ? p.getOptions().stream().map(OptionResponse::from).collect(Collectors.toList()) : List.of())
                .variants(p.getVariants() != null ? p.getVariants().stream().map(VariantResponse::from).collect(Collectors.toList()) : List.of())
                .media(p.getMedia() != null ? p.getMedia().stream().map(MediaResponse::from).collect(Collectors.toList()) : List.of())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class OptionResponse {
        private String publicId;
        private String name;
        private int position;
        private List<String> values;

        public static OptionResponse from(com.securemarts.domain.catalog.entity.ProductOption o) {
            return OptionResponse.builder()
                    .publicId(o.getPublicId())
                    .name(o.getName())
                    .position(o.getPosition())
                    .values(o.getValues() != null ? o.getValues().stream().map(com.securemarts.domain.catalog.entity.ProductOptionValue::getValue).collect(Collectors.toList()) : List.of())
                    .build();
        }
    }

    @Data
    @Builder
    public static class VariantResponse {
        private String publicId;
        private String sku;
        private String title;
        private String barcode;
        private BigDecimal priceAmount;
        private BigDecimal compareAtAmount;
        private String currency;
        private BigDecimal costAmount;
        private BigDecimal weight;
        private String weightUnit;
        private boolean trackInventory;
        private boolean requiresShipping;
        @Schema(description = "Option name → value, e.g. {\"Size\":\"M\",\"Color\":\"Black\"}")
        private Map<String, String> options;
        private int position;
        @Schema(description = "Variant-specific media (e.g. color images). Falls back to product media if empty.")
        private List<MediaResponse> media;

        public static VariantResponse from(ProductVariant v) {
            Map<String, String> optionMap = null;
            if (v.getOptionValues() != null && !v.getOptionValues().isEmpty()) {
                optionMap = v.getOptionValues().stream()
                        .filter(ov -> ov.getOptionValue() != null && ov.getOptionValue().getOption() != null)
                        .collect(Collectors.toMap(ov -> ov.getOptionValue().getOption().getName(), ov -> ov.getOptionValue().getValue(), (a, b) -> a));
            }
            return VariantResponse.builder()
                    .publicId(v.getPublicId())
                    .sku(v.getSku())
                    .title(v.getTitle())
                    .barcode(v.getBarcode())
                    .priceAmount(v.getPriceAmount())
                    .compareAtAmount(v.getCompareAtAmount())
                    .currency(v.getCurrency())
                    .costAmount(v.getCostAmount())
                    .weight(v.getWeight())
                    .weightUnit(v.getWeightUnit())
                    .trackInventory(v.isTrackInventory())
                    .requiresShipping(v.isRequiresShipping())
                    .options(optionMap != null ? optionMap : Map.of())
                    .position(v.getPosition())
                    .media(v.getMedia() != null ? v.getMedia().stream().map(MediaResponse::from).collect(Collectors.toList()) : List.of())
                    .build();
        }
    }

    @Data
    @Builder
    public static class MediaResponse {
        private String publicId;
        private String url;
        private String alt;
        private int position;
        private String mediaType;

        public static MediaResponse from(com.securemarts.domain.catalog.entity.ProductMedia m) {
            return MediaResponse.builder()
                    .publicId(m.getPublicId())
                    .url(m.getUrl())
                    .alt(m.getAlt())
                    .position(m.getPosition())
                    .mediaType(m.getMediaType())
                    .build();
        }
    }
}
