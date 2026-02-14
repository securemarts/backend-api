package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Close POS session")
public class CloseSessionRequest {

    @Schema(description = "Closing cash amount (counted)")
    private BigDecimal closingCashAmount;
}
