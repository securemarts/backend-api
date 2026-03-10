package com.securemarts.domain.order.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.inventory.entity.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * One per (Order, Location). Groups order lines fulfilled from a single location; links to DeliveryOrder when shipped.
 */
@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_order_id", columnList = "order_id"),
        @Index(name = "idx_shipments_order_location", columnList = "order_id, location_id", unique = true),
        @Index(name = "idx_shipments_delivery_order", columnList = "delivery_order_id")
})
@Getter
@Setter
public class Shipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    /** Set when a delivery order is created for this shipment. */
    @Column(name = "delivery_order_id")
    private Long deliveryOrderId;

    public enum ShipmentStatus {
        PENDING,
        FULFILLED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
