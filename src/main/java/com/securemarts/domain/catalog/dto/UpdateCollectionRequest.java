package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Update collection")
public class UpdateCollectionRequest {

    @Size(max = 255)
    @Schema(description = "Collection title")
    private String title;

    @Size(max = 100)
    @Schema(description = "URL handle")
    private String handle;

    @Schema(description = "Description")
    private String description;

    @Pattern(regexp = "^(manual|smart)$", message = "collectionType must be manual or smart")
    @Schema(description = "manual or smart")
    private String collectionType;

    @Pattern(regexp = "^(all|any)$", message = "conditionsOperator must be all or any")
    @Schema(description = "For smart collections: all or any")
    private String conditionsOperator;

    @Schema(description = "Rules for smart collections (replaces existing)")
    private List<CollectionRuleRequest> rules;

    @Schema(description = "Product public IDs for manual collections (optional; when set, replaces membership)")
    private List<String> productIds;
}
