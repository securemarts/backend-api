package com.shopper.domain.pos.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pos_sessions", indexes = {
        @Index(name = "idx_pos_sessions_register", columnList = "register_id"),
        @Index(name = "idx_pos_sessions_status", columnList = "status")
})
@Getter
@Setter
public class POSSession extends BaseEntity {

    @Column(name = "register_id", nullable = false)
    private Long registerId;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "opening_cash_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingCashAmount = BigDecimal.ZERO;

    @Column(name = "closing_cash_amount", precision = 19, scale = 4)
    private BigDecimal closingCashAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.OPEN;

    @Column(name = "opened_by", length = 255)
    private String openedBy;

    public enum SessionStatus {
        OPEN,
        CLOSED
    }
}
