package com.shopper.domain.catalog.dto;

import com.shopper.domain.catalog.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
    private String seoTitle;
    private String seoDescription;
    @Schema(description = "Collection internal ID")
    private Long collectionId;
    @Schema(description = "Store ID; products belong to a store (stores have locations that track inventory)")
    private Long storeId;
    private List<String> tagNames;
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
                .status(p.getStatus().name())
                .seoTitle(p.getSeoTitle())
                .seoDescription(p.getSeoDescription())
                .collectionId(p.getCollectionId())
                .storeId(p.getStoreId())
                .tagNames(p.getTags() != null ? p.getTags().stream().map(com.shopper.domain.catalog.entity.Tag::getName).collect(Collectors.toList()) : List.of())
                .variants(p.getVariants() != null ? p.getVariants().stream().map(VariantResponse::from).collect(Collectors.toList()) : List.of())
                .media(p.getMedia() != null ? p.getMedia().stream().map(MediaResponse::from).collect(Collectors.toList()) : List.of())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class VariantResponse {
        private String publicId;
        private String sku;
        private String title;
        private BigDecimal priceAmount;
        private BigDecimal compareAtAmount;
        private String currency;
        private String attributesJson;
        private int position;

        public static VariantResponse from(com.shopper.domain.catalog.entity.ProductVariant v) {
            return VariantResponse.builder()
                    .publicId(v.getPublicId())
                    .sku(v.getSku())
                    .title(v.getTitle())
                    .priceAmount(v.getPriceAmount())
                    .compareAtAmount(v.getCompareAtAmount())
                    .currency(v.getCurrency())
                    .attributesJson(v.getAttributesJson())
                    .position(v.getPosition())
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

        public static MediaResponse from(com.shopper.domain.catalog.entity.ProductMedia m) {
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
