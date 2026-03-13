package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_purchase_orders_store_id", columnList = "store_id"),
        @Index(name = "idx_purchase_orders_store_status", columnList = "store_id, status"),
        @Index(name = "idx_purchase_orders_supplier_id", columnList = "supplier_id")
}, uniqueConstraints = @UniqueConstraint(name = "idx_purchase_orders_store_number", columnNames = {"store_id", "number"}))
@Getter
@Setter
public class PurchaseOrder extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Location destination;

    @Column(nullable = false, length = 50)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "adjustments_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal adjustmentsCost = BigDecimal.ZERO;

    @Column(name = "tax_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxCost = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "expected_on")
    private LocalDate expectedOn;

    @Column(name = "ordered_at")
    private Instant orderedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "payment_due_on")
    private LocalDate paymentDueOn;

    @Column(nullable = false)
    private boolean paid;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderLineItem> lineItems = new ArrayList<>();

    public enum PurchaseOrderStatus {
        DRAFT,
        ORDERED,
        PARTIAL,
        RECEIVED,
        CANCELLED
    }
}
