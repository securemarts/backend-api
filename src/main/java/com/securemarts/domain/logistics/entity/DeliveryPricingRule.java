package com.securemarts.domain.logistics.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_pricing_rules", indexes = {
        @Index(name = "idx_delivery_pricing_rules_zone", columnList = "zone_id"),
        @Index(name = "idx_delivery_pricing_rules_route", columnList = "origin_hub_id, destination_hub_id")
})
@Getter
@Setter
public class DeliveryPricingRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private DeliveryZone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_hub_id")
    private LogisticsHub originHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_hub_id")
    private LogisticsHub destinationHub;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseAmount;

    @Column(name = "per_kg_amount", precision = 19, scale = 4)
    private BigDecimal perKgAmount;

    @Column(name = "same_city_multiplier", precision = 5, scale = 2)
    private BigDecimal sameCityMultiplier;

    @Column(nullable = false)
    private boolean active = true;
}
