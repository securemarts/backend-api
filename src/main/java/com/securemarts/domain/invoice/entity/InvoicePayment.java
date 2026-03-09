package com.securemarts.domain.invoice.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice_payments", indexes = {
        @Index(name = "idx_invoice_payments_invoice", columnList = "invoice_id")
})
@Getter
@Setter
public class InvoicePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(length = 255)
    private String reference;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    public enum PaymentMethod {
        CASH,
        BANK_TRANSFER,
        POS,
        OTHER
    }
}
