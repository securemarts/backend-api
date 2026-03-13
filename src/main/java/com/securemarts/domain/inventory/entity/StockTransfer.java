package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfers", indexes = {
        @Index(name = "idx_stock_transfers_store_id", columnList = "store_id"),
        @Index(name = "idx_stock_transfers_store_status", columnList = "store_id, status")
}, uniqueConstraints = @UniqueConstraint(name = "idx_stock_transfers_store_number", columnNames = {"store_id", "number"}))
@Getter
@Setter
public class StockTransfer extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_location_id")
    private Location origin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id")
    private Location destination;

    @Column(nullable = false, length = 50)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockTransferStatus status = StockTransferStatus.DRAFT;

    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "reference_name", length = 255)
    private String referenceName;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferLineItem> lineItems = new ArrayList<>();

    public enum StockTransferStatus {
        DRAFT,
        PENDING,
        IN_TRANSIT,
        PARTIAL,
        RECEIVED,
        CANCELLED
    }
}
