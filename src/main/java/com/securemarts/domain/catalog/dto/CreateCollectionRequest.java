package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create collection (manual or smart)")
public class CreateCollectionRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Collection title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 100)
    @Schema(description = "URL handle (auto-generated from title if blank)")
    private String handle;

    @Schema(description = "Description")
    private String description;

    @Pattern(regexp = "^(manual|smart)$", message = "collectionType must be manual or smart")
    @Schema(description = "manual or smart", example = "manual")
    private String collectionType = "manual";

    @Pattern(regexp = "^(all|any)$", message = "conditionsOperator must be all or any")
    @Schema(description = "For smart collections: match all rules (all) or any rule (any)")
    private String conditionsOperator;

    @Schema(description = "Rules for smart collections (field, operator, value)")
    private List<CollectionRuleRequest> rules;

    @Schema(description = "Product public IDs for manual collections (optional at create)")
    private List<String> productIds;
}
