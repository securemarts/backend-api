package com.securemarts.domain.customer.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "store_customers", indexes = {
        @Index(name = "idx_store_customers_store", columnList = "store_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "idx_store_customers_store_phone", columnNames = {"store_id", "phone"})
})
@Getter
@Setter
public class StoreCustomer extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit;
}
