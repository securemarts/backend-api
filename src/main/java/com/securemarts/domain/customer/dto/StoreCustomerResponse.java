package com.securemarts.domain.customer.dto;

import com.securemarts.domain.customer.entity.StoreCustomer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Store customer response")
public class StoreCustomerResponse {

    private String publicId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private BigDecimal creditLimit;
    private Instant createdAt;

    public static StoreCustomerResponse from(StoreCustomer c) {
        return StoreCustomerResponse.builder()
                .publicId(c.getPublicId())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .creditLimit(c.getCreditLimit())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
