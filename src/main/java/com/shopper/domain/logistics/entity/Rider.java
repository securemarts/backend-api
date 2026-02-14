package com.shopper.domain.logistics.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "riders", indexes = {
        @Index(name = "idx_riders_phone", columnList = "phone"),
        @Index(name = "idx_riders_email", columnList = "email"),
        @Index(name = "idx_riders_status", columnList = "status"),
        @Index(name = "idx_riders_zone_available", columnList = "zone_id, is_available")
})
@Getter
@Setter
public class Rider extends BaseEntity {

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RiderStatus status = RiderStatus.OFF_DUTY;

    /** Chowdeck model: rider operates in one service zone; location used for nearest-rider dispatch */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private ServiceZone zone;

    @Column(name = "current_lat", precision = 10, scale = 7)
    private BigDecimal currentLat;

    @Column(name = "current_lng", precision = 10, scale = 7)
    private BigDecimal currentLng;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    public enum RiderStatus {
        AVAILABLE,
        BUSY,
        OFF_DUTY
    }
}
