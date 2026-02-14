package com.shopper.domain.discovery.dto;

import com.shopper.domain.inventory.entity.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Location summary for discovery (public list for a store)")
public class LocationSummaryResponse {

    private String publicId;
    private String name;
    private String address;
    private String city;
    private String state;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static LocationSummaryResponse from(Location loc) {
        return LocationSummaryResponse.builder()
                .publicId(loc.getPublicId())
                .name(loc.getName())
                .address(loc.getAddress())
                .city(loc.getCity())
                .state(loc.getState())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
    }
}
