package com.shopper.domain.onboarding.dto;

import com.shopper.domain.onboarding.entity.MerchantRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Merchant (store staff) role")
public class MerchantRoleResponse {

    private String publicId;
    private String code;
    private String name;
    private String description;
    private Instant createdAt;

    public static MerchantRoleResponse from(MerchantRole r) {
        return MerchantRoleResponse.builder()
                .publicId(r.getPublicId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
