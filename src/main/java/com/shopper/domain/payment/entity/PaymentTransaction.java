package com.shopper.domain.payment.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_transactions_store_id", columnList = "store_id"),
        @Index(name = "idx_payment_transactions_order_id", columnList = "order_id"),
        @Index(name = "idx_payment_transactions_gateway_ref", columnList = "gateway_reference")
})
@Getter
@Setter
public class PaymentTransaction extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, length = 50)
    private String gateway;

    @Column(name = "gateway_reference", length = 255)
    private String gatewayReference;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    public enum PaymentStatus {
        PENDING,
        INITIATED,
        SUCCESS,
        FAILED,
        CANCELLED,
        REFUNDED
    }
}
