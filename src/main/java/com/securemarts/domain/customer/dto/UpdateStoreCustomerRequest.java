package com.securemarts.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request to update a store customer (partial)")
public class UpdateStoreCustomerRequest {

    @Schema(description = "Customer name", example = "Adebayo Johnson")
    private String name;

    @Schema(description = "Customer phone", example = "+2348012345678")
    private String phone;

    @Schema(description = "Customer email", example = "adebayo@example.com")
    private String email;

    @Schema(description = "Customer address", example = "25 Marina Street, Lagos Island, Lagos")
    private String address;

    @Schema(description = "Credit limit for credit sales", example = "50000.00")
    private BigDecimal creditLimit;
}
