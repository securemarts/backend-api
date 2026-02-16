package com.securemarts.domain.logistics.dto;

import com.securemarts.domain.logistics.entity.DeliveryZone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Delivery zone for pricing/assignment")
public class DeliveryZoneResponse {

    private String publicId;
    private String hubPublicId;
    private String name;
    private String state;
    private String city;
    private BigDecimal radiusKm;
    private boolean active;
    private Instant createdAt;

    public static DeliveryZoneResponse from(DeliveryZone zone) {
        return DeliveryZoneResponse.builder()
                .publicId(zone.getPublicId())
                .hubPublicId(zone.getHub() != null ? zone.getHub().getPublicId() : null)
                .name(zone.getName())
                .state(zone.getState())
                .city(zone.getCity())
                .radiusKm(zone.getRadiusKm())
                .active(zone.isActive())
                .createdAt(zone.getCreatedAt())
                .build();
    }
}
