package com.shopper.domain.logistics.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "logistics_hubs", indexes = {
        @Index(name = "idx_logistics_hubs_state", columnList = "state"),
        @Index(name = "idx_logistics_hubs_city", columnList = "city"),
        @Index(name = "idx_logistics_hubs_state_city", columnList = "state, city")
})
@Getter
@Setter
public class LogisticsHub extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 500)
    private String address;

    @Column(nullable = false)
    private boolean active = true;
}
