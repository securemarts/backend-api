package com.securemarts.domain.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Schema(description = "Create or update price rule (promotion)")
public class PriceRuleRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank
    @Schema(description = "Discount value type", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"FIXED_AMOUNT", "PERCENTAGE"})
    private String valueType;

    @Schema(description = "Fixed amount (when valueType=FIXED_AMOUNT)")
    private BigDecimal valueAmount;

    @Schema(description = "Percentage 0-100 (when valueType=PERCENTAGE)")
    private BigDecimal valuePercent;

    @Schema(description = "Starts at (ISO-8601)")
    private Instant startsAt;

    @Schema(description = "Ends at (ISO-8601)")
    private Instant endsAt;

    @Schema(description = "Max number of uses (null = unlimited)")
    private Integer usageLimit;
}
