package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_product_variants_product_id", columnList = "product_id")
})
@Getter
@Setter
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 100)
    private String sku;

    @Column(length = 255)
    private String title;

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "compare_at_amount", precision = 19, scale = 4)
    private BigDecimal compareAtAmount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Column(name = "attributes_json", columnDefinition = "TEXT")
    private String attributesJson;

    @Column(nullable = false)
    private int position;
}
