package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(length = 100)
    private String barcode;

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "compare_at_amount", precision = 19, scale = 4)
    private BigDecimal compareAtAmount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Column(name = "cost_amount", precision = 12, scale = 2)
    private BigDecimal costAmount;

    @Column(precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit;

    @Column(name = "track_inventory", nullable = false)
    private boolean trackInventory = true;

    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;

    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantOptionValue> optionValues = new ArrayList<>();

    /**
     * Optional variant-specific media (e.g. color images). Product-level media stays on Product.media.
     */
    @ManyToMany
    @JoinTable(
            name = "product_variant_media",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id")
    )
    private Set<ProductMedia> media = new HashSet<>();
}
