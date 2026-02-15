package com.shopper.domain.pos.dto;

import com.shopper.domain.pos.entity.POSSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "POS session (open/close shift)")
public class POSSessionResponse {

    private String publicId;
    private String registerId;
    private Instant openedAt;
    private Instant closedAt;
    private BigDecimal openingCashAmount;
    private BigDecimal closingCashAmount;
    @Schema(description = "Session status", allowableValues = {"OPEN", "CLOSED"})
    private String status;
    private String openedBy;
    private Instant createdAt;

    public static POSSessionResponse from(POSSession s) {
        return POSSessionResponse.builder()
                .publicId(s.getPublicId())
                .registerId(null)
                .openedAt(s.getOpenedAt())
                .closedAt(s.getClosedAt())
                .openingCashAmount(s.getOpeningCashAmount())
                .closingCashAmount(s.getClosingCashAmount())
                .status(s.getStatus().name())
                .openedBy(s.getOpenedBy())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
