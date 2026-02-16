package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.MerchantPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Merchant (store) permission")
public class MerchantPermissionResponse {

    private String publicId;
    private String code;
    private String description;
    private Instant createdAt;

    public static MerchantPermissionResponse from(MerchantPermission p) {
        return MerchantPermissionResponse.builder()
                .publicId(p.getPublicId())
                .code(p.getCode())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
