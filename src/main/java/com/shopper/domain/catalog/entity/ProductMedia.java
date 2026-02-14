package com.shopper.domain.catalog.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_media", indexes = {
        @Index(name = "idx_product_media_product_id", columnList = "product_id")
})
@Getter
@Setter
public class ProductMedia extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String alt;

    @Column(nullable = false)
    private int position;

    @Column(name = "media_type", length = 20)
    private String mediaType = "image";
}
