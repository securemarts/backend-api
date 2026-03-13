package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Smart collection rule condition")
public class CollectionRuleRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Field: title, product_type, vendor, tag, price, compare_at_price, weight, inventory_stock, variant_title", example = "tag")
    private String field;

    @NotBlank
    @Size(max = 30)
    @Schema(description = "Operator: equals, not_equals, greater_than, less_than, contains, starts_with, ends_with", example = "equals")
    private String operator;

    @Schema(description = "Value to compare (string or number)")
    private String value;
}
