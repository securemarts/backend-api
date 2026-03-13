package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory_levels", indexes = {
        @Index(name = "idx_inventory_levels_item", columnList = "inventory_item_id"),
        @Index(name = "idx_inventory_levels_location", columnList = "location_id")
}, uniqueConstraints = @UniqueConstraint(name = "uq_inventory_level_item_location", columnNames = {"inventory_item_id", "location_id"}))
@Getter
@Setter
public class InventoryLevel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    @Column(name = "quantity_incoming", nullable = false)
    private int quantityIncoming;

    public int getQuantityOnHand() {
        return quantityAvailable + quantityReserved;
    }
}
