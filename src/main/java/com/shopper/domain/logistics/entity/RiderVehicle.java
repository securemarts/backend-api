package com.shopper.domain.logistics.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "rider_vehicles", indexes = {
        @Index(name = "idx_rider_vehicles_rider", columnList = "rider_id")
})
@Getter
@Setter
public class RiderVehicle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    @Column(name = "plate_number", length = 30)
    private String plateNumber;

    @Column(name = "capacity_weight_kg", precision = 10, scale = 2)
    private BigDecimal capacityWeightKg;

    public enum VehicleType {
        BIKE,
        CAR,
        VAN
    }
}
