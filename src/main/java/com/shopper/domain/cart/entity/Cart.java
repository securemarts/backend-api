package com.shopper.domain.cart.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_store_id", columnList = "store_id"),
        @Index(name = "idx_carts_cart_token", columnList = "cart_token")
})
@Getter
@Setter
public class Cart extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "cart_token", length = 64)
    private String cartToken;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
