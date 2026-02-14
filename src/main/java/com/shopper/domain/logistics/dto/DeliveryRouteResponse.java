package com.shopper.domain.logistics.dto;

import com.shopper.domain.logistics.entity.DeliveryRoute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Delivery route (origin hub to destination hub)")
public class DeliveryRouteResponse {

    private String publicId;
    private String originHubPublicId;
    private String originHubName;
    private String destinationHubPublicId;
    private String destinationHubName;
    private BigDecimal estimatedHours;
    private boolean active;
    private Instant createdAt;

    public static DeliveryRouteResponse from(DeliveryRoute route) {
        return DeliveryRouteResponse.builder()
                .publicId(route.getPublicId())
                .originHubPublicId(route.getOriginHub().getPublicId())
                .originHubName(route.getOriginHub().getName())
                .destinationHubPublicId(route.getDestinationHub().getPublicId())
                .destinationHubName(route.getDestinationHub().getName())
                .estimatedHours(route.getEstimatedHours())
                .active(route.isActive())
                .createdAt(route.getCreatedAt())
                .build();
    }
}
