package com.securemarts.domain.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "sync_logs", indexes = {
        @Index(name = "idx_sync_logs_register", columnList = "register_id")
})
@Getter
@Setter
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String publicId = java.util.UUID.randomUUID().toString();

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "register_id", nullable = false)
    private Long registerId;

    @Column(name = "client_sync_token", length = 100)
    private String clientSyncToken;

    @Column(name = "server_sync_token", length = 100)
    private String serverSyncToken;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt = Instant.now();

    @Column(name = "conflict_count")
    private Integer conflictCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (publicId == null) publicId = java.util.UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }
}
