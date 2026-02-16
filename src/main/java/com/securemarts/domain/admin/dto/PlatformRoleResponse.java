package com.securemarts.domain.admin.dto;

import com.securemarts.domain.admin.entity.PlatformRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Platform (admin) role")
public class PlatformRoleResponse {

    private String publicId;
    private String code;
    private String name;
    private String description;
    private Instant createdAt;

    public static PlatformRoleResponse from(PlatformRole r) {
        return PlatformRoleResponse.builder()
                .publicId(r.getPublicId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
