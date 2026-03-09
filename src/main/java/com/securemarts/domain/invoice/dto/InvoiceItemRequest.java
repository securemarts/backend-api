package com.securemarts.domain.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Invoice line item (variant or free-text description)")
public class InvoiceItemRequest {

    @Schema(description = "Product variant public ID (optional; if null, description is required)")
    private String variantPublicId;

    @Schema(description = "Line description (required when variantPublicId is null)")
    private String description;

    @NotNull
    @Min(1)
    @Schema(description = "Quantity", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @NotNull
    @DecimalMin("0")
    @Schema(description = "Unit price", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitPrice;
}
