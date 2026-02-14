package com.shopper.domain.logistics.dto;

import com.shopper.domain.logistics.entity.ServiceZone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Service zone (Chowdeck: radius-based delivery area with pricing)")
public class ServiceZoneResponse {

    private String publicId;
    private String name;
    private String city;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private BigDecimal radiusKm;
    private BigDecimal baseFee;
    private BigDecimal perKmFee;
    private BigDecimal maxDistanceKm;
    private BigDecimal minOrderAmount;
    private boolean surgeEnabled;
    private boolean active;
    private Instant createdAt;

    public static ServiceZoneResponse from(ServiceZone z) {
        return ServiceZoneResponse.builder()
                .publicId(z.getPublicId())
                .name(z.getName())
                .city(z.getCity())
                .centerLat(z.getCenterLat())
                .centerLng(z.getCenterLng())
                .radiusKm(z.getRadiusKm())
                .baseFee(z.getBaseFee())
                .perKmFee(z.getPerKmFee())
                .maxDistanceKm(z.getMaxDistanceKm())
                .minOrderAmount(z.getMinOrderAmount())
                .surgeEnabled(z.isSurgeEnabled())
                .active(z.isActive())
                .createdAt(z.getCreatedAt())
                .build();
    }
}
