package com.securemarts.domain.favorite.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.auth.entity.User;
import com.securemarts.domain.catalog.entity.Product;
import com.securemarts.domain.onboarding.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer_favorites", indexes = {
        @Index(name = "idx_customer_favorites_user_id", columnList = "user_id"),
        @Index(name = "idx_customer_favorites_store_id", columnList = "store_id"),
        @Index(name = "idx_customer_favorites_product_id", columnList = "product_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_favorites_user_store_product", columnNames = {"user_id", "store_id", "product_id"})
})
@Getter
@Setter
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
