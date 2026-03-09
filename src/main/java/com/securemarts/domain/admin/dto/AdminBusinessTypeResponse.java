package com.securemarts.domain.admin.dto;

import com.securemarts.domain.onboarding.entity.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Admin view of a business type")
public class AdminBusinessTypeResponse {

    private String publicId;
    private String code;
    private String name;
    private String description;
    private String iconKey;

    public static AdminBusinessTypeResponse from(BusinessType t) {
        return AdminBusinessTypeResponse.builder()
                .publicId(t.getPublicId())
                .code(t.getCode())
                .name(t.getName())
                .description(t.getDescription())
                .iconKey(t.getIconKey())
                .build();
    }
}

