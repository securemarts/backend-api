package com.securemarts.domain.onboarding.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts", indexes = {
        @Index(name = "idx_bank_accounts_store_id", columnList = "store_id")
})
@Getter
@Setter
public class BankAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 10)
    private String bankCode;

    @Column(nullable = false, length = 255)
    private String bankName;

    @Column(nullable = false, length = 10)
    private String accountNumber;

    @Column(nullable = false, length = 100)
    private String accountName;

    @Column(nullable = false)
    private boolean payoutDefault;
}
