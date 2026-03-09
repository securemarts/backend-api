package com.securemarts.domain.onboarding.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_stores_business_id", columnList = "business_id"),
        @Index(name = "idx_stores_domain_slug", columnList = "domain_slug"),
        @Index(name = "idx_stores_sales_channel", columnList = "sales_channel")
})
@Getter
@Setter
public class Store extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "domain_slug", nullable = false, unique = true, length = 100)
    private String domainSlug;

    @Column(name = "default_currency", length = 3)
    private String defaultCurrency = "NGN";

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_channel", nullable = false, length = 20)
    private SalesChannel salesChannel = SalesChannel.BOTH;

    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private StoreProfile profile;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankAccount> bankAccounts = new ArrayList<>();

    public boolean isPaymentsEnabled() {
        return active && business.getVerificationStatus() == Business.VerificationStatus.APPROVED;
    }

    public boolean isOnlineEnabled() {
        return salesChannel == SalesChannel.ONLINE || salesChannel == SalesChannel.BOTH;
    }

    public boolean isRetailEnabled() {
        return salesChannel == SalesChannel.RETAIL || salesChannel == SalesChannel.BOTH;
    }

    public enum SalesChannel {
        ONLINE,
        RETAIL,
        BOTH
    }
}
