package com.shopper.domain.logistics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "delivery_tracking_events", indexes = {
        @Index(name = "idx_delivery_tracking_events_delivery", columnList = "delivery_order_id"),
        @Index(name = "idx_delivery_tracking_events_created", columnList = "delivery_order_id, created_at")
})
@Getter
@Setter
public class DeliveryTrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String publicId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id", nullable = false)
    private DeliveryOrder deliveryOrder;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (publicId == null) publicId = java.util.UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }
}
