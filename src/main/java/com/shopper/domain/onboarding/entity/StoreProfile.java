package com.shopper.domain.onboarding.entity;

import com.shopper.common.entity.BaseEntity;
import com.shopper.domain.logistics.entity.ServiceZone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "store_profiles", indexes = {
        @Index(name = "idx_store_profiles_store_id", columnList = "store_id"),
        @Index(name = "idx_store_profiles_state_city", columnList = "state, city"),
        @Index(name = "idx_store_profiles_lat_lng", columnList = "latitude, longitude"),
        @Index(name = "idx_store_profiles_zone", columnList = "zone_id")
})
@Getter
@Setter
public class StoreProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    /** Service zone this store delivers in (Chowdeck model) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private ServiceZone zone;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 2)
    private String country = "NG";

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
}
