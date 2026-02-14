package com.shopper.domain.inventory.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_locations_store_id", columnList = "store_id"),
        @Index(name = "idx_locations_store_city", columnList = "store_id, city"),
        @Index(name = "idx_locations_lat_lng", columnList = "latitude, longitude")
})
@Getter
@Setter
public class Location extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
}
