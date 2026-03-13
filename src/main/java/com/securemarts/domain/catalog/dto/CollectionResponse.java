package com.securemarts.domain.catalog.dto;

import com.securemarts.domain.catalog.entity.Collection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(description = "Collection")
public class CollectionResponse {

    @Schema(description = "Public ID")
    private String publicId;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Handle")
    private String handle;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "manual or smart")
    private String collectionType;

    @Schema(description = "For smart: all or any")
    private String conditionsOperator;

    @Schema(description = "Image URL")
    private String imageUrl;

    @Schema(description = "Rules (smart collections only)")
    private List<CollectionRuleResponse> rules;

    @Schema(description = "Product count in collection")
    private Long productCount;

    public static CollectionResponse from(Collection c) {
        return from(c, null);
    }

    public static CollectionResponse from(Collection c, Long productCount) {
        CollectionResponse r = new CollectionResponse();
        r.setPublicId(c.getPublicId());
        r.setTitle(c.getTitle());
        r.setHandle(c.getHandle());
        r.setDescription(c.getDescription());
        r.setCollectionType(c.getCollectionType() != null ? c.getCollectionType().name().toLowerCase() : "manual");
        r.setConditionsOperator(c.getConditionsOperator());
        r.setImageUrl(c.getImageUrl());
        r.setRules(c.getRules() != null ? c.getRules().stream().map(CollectionRuleResponse::from).collect(Collectors.toList()) : null);
        r.setProductCount(productCount);
        return r;
    }
}
