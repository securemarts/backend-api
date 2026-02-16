package com.securemarts.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Current cash drawer state (derived from movements)")
public class CashDrawerResponse {

    private BigDecimal balance;
    private String sessionPublicId;
    @Schema(description = "Session status", allowableValues = {"OPEN", "CLOSED"})
    private String status;
}
