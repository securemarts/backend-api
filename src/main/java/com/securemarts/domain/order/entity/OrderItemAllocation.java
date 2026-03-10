package com.securemarts.domain.order.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.inventory.entity.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Allocates quantity of an order line to a specific location. One order line can be split across locations.
 */
@Entity
@Table(name = "order_item_allocations", indexes = {
        @Index(name = "idx_order_item_allocations_order_item", columnList = "order_item_id"),
        @Index(name = "idx_order_item_allocations_location", columnList = "location_id")
})
@Getter
@Setter
public class OrderItemAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private int quantity;
}
