package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Schema(description = "Offline transaction (for sync)")
public class OfflineTransactionDto {

    @NotBlank
    private String clientId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal amount;

    @NotBlank
    private String currency = "NGN";

    private Instant clientCreatedAt;

    @Valid
    private List<OfflineTransactionItemDto> items;
}
