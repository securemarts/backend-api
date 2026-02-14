package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Open POS session")
public class OpenSessionRequest {

    @Schema(description = "Opening cash amount")
    private BigDecimal openingCashAmount = BigDecimal.ZERO;

    private String openedBy;
}
