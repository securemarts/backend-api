package com.securemarts.domain.inventory.dto;

import com.securemarts.domain.inventory.entity.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Supplier details")
public class SupplierResponse {

    private String publicId;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String notes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static SupplierResponse from(Supplier s) {
        return SupplierResponse.builder()
                .publicId(s.getPublicId())
                .name(s.getName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .company(s.getCompany())
                .address1(s.getAddress1())
                .address2(s.getAddress2())
                .city(s.getCity())
                .state(s.getState())
                .country(s.getCountry())
                .postalCode(s.getPostalCode())
                .notes(s.getNotes())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
