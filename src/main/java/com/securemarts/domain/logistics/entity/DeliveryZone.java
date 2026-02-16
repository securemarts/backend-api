package com.securemarts.domain.logistics.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_zones", indexes = {
        @Index(name = "idx_delivery_zones_hub", columnList = "hub_id"),
        @Index(name = "idx_delivery_zones_state_city", columnList = "state, city")
})
@Getter
@Setter
public class DeliveryZone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id")
    private LogisticsHub hub;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(name = "radius_km", precision = 8, scale = 2)
    private BigDecimal radiusKm;

    @Column(nullable = false)
    private boolean active = true;
}
