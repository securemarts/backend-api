package com.securemarts.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Line item for offline transaction")
public class OfflineTransactionItemDto {

    @NotBlank
    private String productVariantPublicId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin("0")
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0")
    private BigDecimal totalPrice;
}
