package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Business type")
public class BusinessTypeResponse {

    private String publicId;
    private String code;
    private String name;
    private String description;
    private String iconKey;

    public static BusinessTypeResponse from(BusinessType t) {
        return BusinessTypeResponse.builder()
                .publicId(t.getPublicId())
                .code(t.getCode())
                .name(t.getName())
                .description(t.getDescription())
                .iconKey(t.getIconKey())
                .build();
    }
}

