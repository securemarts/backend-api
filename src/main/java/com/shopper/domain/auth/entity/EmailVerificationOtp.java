package com.shopper.domain.auth.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "email_verification_otps", indexes = {
        @Index(name = "idx_email_verification_otp_email_type", columnList = "email, target_type"),
        @Index(name = "idx_email_verification_otp_expires", columnList = "expires_at")
})
@Getter
@Setter
public class EmailVerificationOtp extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "otp_hash", nullable = false, length = 64)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }

    public enum TargetType {
        USER,
        RIDER
    }
}
