package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stock_transfer_line_items", indexes = {
        @Index(name = "idx_st_line_items_transfer_id", columnList = "stock_transfer_id"),
        @Index(name = "idx_st_line_items_inventory_item", columnList = "inventory_item_id")
})
@Getter
@Setter
public class StockTransferLineItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_transfer_id", nullable = false)
    private StockTransfer stockTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    private int rejectedQuantity;
}
