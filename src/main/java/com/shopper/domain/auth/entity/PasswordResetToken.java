package com.shopper.domain.auth.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_reset_tokens_token", columnList = "token_hash"),
        @Index(name = "idx_reset_tokens_user_id", columnList = "user_id")
})
@Getter
@Setter
public class PasswordResetToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant usedAt;

    public boolean isValid() {
        return usedAt == null && Instant.now().isBefore(expiresAt);
    }
}
