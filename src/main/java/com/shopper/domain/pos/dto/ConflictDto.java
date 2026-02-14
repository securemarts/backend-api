package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Conflict on sync")
public class ConflictDto {

    private String clientId;
    private String reason;
}
