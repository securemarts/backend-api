package com.securemarts.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Open POS session")
public class OpenSessionRequest {

    @Schema(description = "Opening cash amount", example = "5000.00")
    private BigDecimal openingCashAmount = BigDecimal.ZERO;

    @Schema(description = "Staff name or ID who opened the session", example = "John")
    private String openedBy;
}
