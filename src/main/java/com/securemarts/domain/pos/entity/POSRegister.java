package com.securemarts.domain.pos.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pos_registers", indexes = {
        @Index(name = "idx_pos_registers_store", columnList = "store_id")
})
@Getter
@Setter
public class POSRegister extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(nullable = false)
    private boolean active = true;
}
