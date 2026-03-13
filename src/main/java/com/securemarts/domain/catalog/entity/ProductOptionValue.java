package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_option_values", indexes = {
        @Index(name = "idx_product_option_values_option_id", columnList = "option_id")
})
@Getter
@Setter
public class ProductOptionValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption option;

    @Column(nullable = false, length = 100)
    private String value;

    @Column(nullable = false)
    private int position;
}
