package com.shopper.domain.admin.dto;

import com.shopper.domain.admin.entity.AdminPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Admin (platform) permission")
public class AdminPermissionResponse {

    private String publicId;
    private String code;
    private String description;
    private Instant createdAt;

    public static AdminPermissionResponse from(AdminPermission p) {
        return AdminPermissionResponse.builder()
                .publicId(p.getPublicId())
                .code(p.getCode())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
