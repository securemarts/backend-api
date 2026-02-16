package com.securemarts.domain.logistics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "proof_of_delivery", indexes = {
        @Index(name = "idx_proof_of_delivery_delivery", columnList = "delivery_order_id")
})
@Getter
@Setter
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String publicId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id", nullable = false)
    private DeliveryOrder deliveryOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProofType type;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (publicId == null) publicId = java.util.UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum ProofType {
        SIGNATURE,
        PHOTO
    }
}
