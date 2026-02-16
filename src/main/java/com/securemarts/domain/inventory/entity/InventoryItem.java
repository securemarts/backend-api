package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory_items", indexes = {
        @Index(name = "idx_inventory_items_store_id", columnList = "store_id"),
        @Index(name = "idx_inventory_items_variant_location", columnList = "product_variant_id, location_id", unique = true)
})
@Getter
@Setter
public class InventoryItem extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    public int getQuantityOnHand() {
        return quantityAvailable + quantityReserved;
    }

    public boolean isLowStock() {
        return lowStockThreshold != null && quantityAvailable <= lowStockThreshold;
    }
}
