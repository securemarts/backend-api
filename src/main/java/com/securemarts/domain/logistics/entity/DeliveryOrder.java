package com.securemarts.domain.logistics.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_orders", indexes = {
        @Index(name = "idx_delivery_orders_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_delivery_orders_store", columnList = "store_id"),
        @Index(name = "idx_delivery_orders_rider", columnList = "rider_id"),
        @Index(name = "idx_delivery_orders_status", columnList = "status"),
        @Index(name = "idx_delivery_orders_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_delivery_orders_batch", columnList = "batch_id")
})
@Getter
@Setter
public class DeliveryOrder extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_hub_id")
    private LogisticsHub originHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_hub_id")
    private LogisticsHub destinationHub;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    /** Customer delivery location (for zone check and fee calculation) */
    @Column(name = "delivery_lat", precision = 10, scale = 7)
    private BigDecimal deliveryLat;

    @Column(name = "delivery_lng", precision = 10, scale = 7)
    private BigDecimal deliveryLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider;

    @Column(name = "pricing_amount", precision = 19, scale = 4)
    private BigDecimal pricingAmount;

    @Column(name = "pricing_currency", length = 3)
    private String pricingCurrency = "NGN";

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    @Column(name = "batch_id", length = 36)
    private String batchId;

    @Version
    private int version;

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryTrackingEvent> trackingEvents = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProofOfDelivery> proofOfDeliveries = new ArrayList<>();

    public enum DeliveryStatus {
        PENDING,
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        DELIVERED,
        FAILED,
        RETURNED
    }
}
