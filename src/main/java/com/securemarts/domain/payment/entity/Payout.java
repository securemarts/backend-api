package com.securemarts.domain.payment.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payouts", indexes = {
        @Index(name = "idx_payouts_store_id", columnList = "store_id")
})
@Getter
@Setter
public class Payout extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "gateway_reference", length = 255)
    private String gatewayReference;

    @Column(name = "paid_at")
    private Instant paidAt;

    public enum PayoutStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
