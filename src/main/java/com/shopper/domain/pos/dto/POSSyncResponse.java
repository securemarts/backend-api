package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "POS sync response (accepted and conflicts)")
public class POSSyncResponse {

    private List<SyncedTransactionDto> accepted;
    private List<ConflictDto> conflicts;
    private String serverSyncToken;
}
