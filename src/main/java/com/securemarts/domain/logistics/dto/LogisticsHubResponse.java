package com.securemarts.domain.logistics.dto;

import com.securemarts.domain.logistics.entity.LogisticsHub;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Logistics hub (hub-and-spoke)")
public class LogisticsHubResponse {

    private String publicId;
    private String state;
    private String city;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private boolean active;
    private Instant createdAt;

    public static LogisticsHubResponse from(LogisticsHub hub) {
        return LogisticsHubResponse.builder()
                .publicId(hub.getPublicId())
                .state(hub.getState())
                .city(hub.getCity())
                .name(hub.getName())
                .latitude(hub.getLatitude())
                .longitude(hub.getLongitude())
                .address(hub.getAddress())
                .active(hub.isActive())
                .createdAt(hub.getCreatedAt())
                .build();
    }
}
