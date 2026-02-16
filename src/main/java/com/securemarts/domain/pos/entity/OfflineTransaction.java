package com.securemarts.domain.pos.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offline_transactions", indexes = {
        @Index(name = "idx_offline_tx_register_client", columnList = "register_id, client_id", unique = true),
        @Index(name = "idx_offline_transactions_store", columnList = "store_id"),
        @Index(name = "idx_offline_transactions_session", columnList = "session_id")
})
@Getter
@Setter
public class OfflineTransaction extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "register_id", nullable = false)
    private Long registerId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Column(name = "client_created_at")
    private Instant clientCreatedAt;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @Version
    private int version;

    @OneToMany(mappedBy = "offlineTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfflineTransactionItem> items = new ArrayList<>();
}
