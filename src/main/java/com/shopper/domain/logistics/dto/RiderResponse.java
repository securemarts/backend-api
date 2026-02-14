package com.shopper.domain.logistics.dto;

import com.shopper.domain.logistics.entity.Rider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Rider (Chowdeck: zone, location, availability)")
public class RiderResponse {

    private String publicId;
    private String phone;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    private String zonePublicId;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private boolean available;
    private Instant createdAt;

    public static RiderResponse from(Rider r) {
        return RiderResponse.builder()
                .publicId(r.getPublicId())
                .phone(r.getPhone())
                .email(r.getEmail())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .status(r.getStatus().name())
                .zonePublicId(r.getZone() != null ? r.getZone().getPublicId() : null)
                .currentLat(r.getCurrentLat())
                .currentLng(r.getCurrentLng())
                .available(r.isAvailable())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
