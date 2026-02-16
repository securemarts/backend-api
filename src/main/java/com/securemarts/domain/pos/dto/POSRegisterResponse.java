package com.securemarts.domain.pos.dto;

import com.securemarts.domain.pos.entity.POSRegister;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "POS register")
public class POSRegisterResponse {

    private String publicId;
    private Long storeId;
    private Long locationId;
    private String name;
    private String deviceId;
    private boolean active;
    private Instant createdAt;

    public static POSRegisterResponse from(POSRegister r) {
        return POSRegisterResponse.builder()
                .publicId(r.getPublicId())
                .storeId(r.getStoreId())
                .locationId(r.getLocationId())
                .name(r.getName())
                .deviceId(r.getDeviceId())
                .active(r.isActive())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
