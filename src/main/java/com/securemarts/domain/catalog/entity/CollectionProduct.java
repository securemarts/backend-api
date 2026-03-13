package com.securemarts.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "collection_products", indexes = {
        @Index(name = "idx_collection_products_collection_id", columnList = "collection_id"),
        @Index(name = "idx_collection_products_product_id", columnList = "product_id")
})
@Getter
@Setter
@IdClass(CollectionProductId.class)
public class CollectionProduct {

    @Id
    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false, insertable = false, updatable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, insertable = false, updatable = false)
    private Product product;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
