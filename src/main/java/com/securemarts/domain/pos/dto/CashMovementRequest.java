package com.securemarts.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Cash movement (withdrawal/deposit)")
public class CashMovementRequest {

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Movement amount", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Movement type", allowableValues = {"OPENING", "SALE", "WITHDRAWAL", "DEPOSIT", "CLOSING"}, example = "WITHDRAWAL")
    private String type;

    @Schema(description = "Reason for movement", example = "Petty cash withdrawal")
    private String reason;
}
