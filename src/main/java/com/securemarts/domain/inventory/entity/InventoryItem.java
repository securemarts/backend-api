package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_items", indexes = {
        @Index(name = "idx_inventory_items_store_id", columnList = "store_id"),
        @Index(name = "idx_inventory_items_store_variant", columnList = "store_id, product_variant_id", unique = true),
        @Index(name = "idx_inventory_items_variant_id", columnList = "product_variant_id")
})
@Getter
@Setter
public class InventoryItem extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private boolean tracked = true;

    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;

    @Column(name = "cost_amount", precision = 12, scale = 2)
    private BigDecimal costAmount;

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryLevel> levels = new ArrayList<>();
}
