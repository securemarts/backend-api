package com.securemarts.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request to create a store customer")
public class CreateStoreCustomerRequest {

    @NotBlank
    @Schema(description = "Customer name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Adebayo Johnson")
    private String name;

    @NotBlank
    @Schema(description = "Customer phone", requiredMode = Schema.RequiredMode.REQUIRED, example = "+2348012345678")
    private String phone;

    @Schema(description = "Customer email", example = "adebayo@example.com")
    private String email;

    @Schema(description = "Customer address", example = "25 Marina Street, Lagos Island, Lagos")
    private String address;

    @Schema(description = "Optional credit limit for credit sales", example = "50000.00")
    private BigDecimal creditLimit;
}
