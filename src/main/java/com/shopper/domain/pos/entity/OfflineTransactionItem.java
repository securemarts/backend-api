package com.shopper.domain.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "offline_transaction_items", indexes = {
        @Index(name = "idx_offline_tx_items_tx", columnList = "offline_transaction_id")
})
@Getter
@Setter
public class OfflineTransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offline_transaction_id", nullable = false)
    private OfflineTransaction offlineTransaction;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
