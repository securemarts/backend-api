package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Accepted synced transaction")
public class SyncedTransactionDto {

    private String clientId;
    private String publicId;
    private int version;
}
