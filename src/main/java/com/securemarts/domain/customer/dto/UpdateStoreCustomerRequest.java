package com.securemarts.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request to update a store customer (partial)")
public class UpdateStoreCustomerRequest {

    @Schema(description = "Customer name")
    private String name;

    @Schema(description = "Customer phone")
    private String phone;

    @Schema(description = "Customer email")
    private String email;

    @Schema(description = "Customer address")
    private String address;

    @Schema(description = "Credit limit for credit sales")
    private BigDecimal creditLimit;
}
