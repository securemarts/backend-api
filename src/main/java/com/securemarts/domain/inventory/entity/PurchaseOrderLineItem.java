package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_line_items", indexes = {
        @Index(name = "idx_po_line_items_po_id", columnList = "purchase_order_id"),
        @Index(name = "idx_po_line_items_inventory_item", columnList = "inventory_item_id")
})
@Getter
@Setter
public class PurchaseOrderLineItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

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

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "retail_price", precision = 12, scale = 2)
    private BigDecimal retailPrice;
}
