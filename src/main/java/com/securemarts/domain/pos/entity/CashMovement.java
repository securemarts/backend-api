package com.securemarts.domain.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_movements", indexes = {
        @Index(name = "idx_cash_movements_session", columnList = "session_id")
})
@Getter
@Setter
public class CashMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String publicId = java.util.UUID.randomUUID().toString();

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (publicId == null) publicId = java.util.UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum MovementType {
        OPENING,
        SALE,
        WITHDRAWAL,
        DEPOSIT,
        CLOSING
    }
}
