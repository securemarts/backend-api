package com.shopper.domain.logistics.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "service_zones", indexes = {
        @Index(name = "idx_service_zones_city", columnList = "city"),
        @Index(name = "idx_service_zones_active", columnList = "active")
})
@Getter
@Setter
public class ServiceZone extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(name = "center_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal centerLat;

    @Column(name = "center_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal centerLng;

    @Column(name = "radius_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal radiusKm;

    @Column(name = "base_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseFee;

    @Column(name = "per_km_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal perKmFee;

    @Column(name = "max_distance_km", precision = 8, scale = 2)
    private BigDecimal maxDistanceKm;

    @Column(name = "min_order_amount", precision = 19, scale = 4)
    private BigDecimal minOrderAmount;

    @Column(name = "surge_enabled", nullable = false)
    private boolean surgeEnabled = false;

    @Column(nullable = false)
    private boolean active = true;
}
