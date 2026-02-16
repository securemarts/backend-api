package com.securemarts.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "POS sync request (batch of offline transactions)")
public class POSSyncRequest {

    @Valid
    private List<OfflineTransactionDto> transactions;

    @Schema(description = "Client sync token for conflict detection")
    private String clientSyncToken;
}
