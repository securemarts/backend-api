package com.securemarts.domain.rider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "rider_refresh_tokens", indexes = {
        @Index(name = "idx_rider_refresh_tokens_jti", columnList = "token_jti", unique = true),
        @Index(name = "idx_rider_refresh_tokens_rider", columnList = "rider_id"),
        @Index(name = "idx_rider_refresh_tokens_expires", columnList = "expires_at")
})
@Getter
@Setter
public class RiderRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rider_id", nullable = false)
    private Long riderId;

    @Column(name = "token_jti", nullable = false, unique = true, length = 36)
    private String tokenJti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
