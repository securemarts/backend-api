package com.securemarts.domain.logistics.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_routes", indexes = {
        @Index(name = "idx_delivery_routes_origin", columnList = "origin_hub_id"),
        @Index(name = "idx_delivery_routes_destination", columnList = "destination_hub_id"),
        @Index(name = "idx_delivery_routes_origin_dest", columnList = "origin_hub_id, destination_hub_id", unique = true)
})
@Getter
@Setter
public class DeliveryRoute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_hub_id", nullable = false)
    private LogisticsHub originHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_hub_id", nullable = false)
    private LogisticsHub destinationHub;

    @Column(name = "estimated_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal estimatedHours;

    @Column(nullable = false)
    private boolean active = true;
}
