package com.securemarts.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request to create a store customer")
public class CreateStoreCustomerRequest {

    @NotBlank
    @Schema(description = "Customer name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Customer phone", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Customer email")
    private String email;

    @Schema(description = "Customer address")
    private String address;

    @Schema(description = "Optional credit limit for credit sales")
    private BigDecimal creditLimit;
}
