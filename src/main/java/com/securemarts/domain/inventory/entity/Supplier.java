package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_suppliers_store_id", columnList = "store_id"),
        @Index(name = "idx_suppliers_store_active", columnList = "store_id, active")
})
@Getter
@Setter
public class Supplier extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String company;

    @Column(length = 500)
    private String address1;

    @Column(length = 500)
    private String address2;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;
}
