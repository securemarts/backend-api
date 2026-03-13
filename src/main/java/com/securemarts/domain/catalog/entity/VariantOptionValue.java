package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variant_option_values", indexes = {
        @Index(name = "idx_variant_option_values_variant", columnList = "variant_id"),
        @Index(name = "idx_variant_option_values_option_value", columnList = "option_value_id")
}, uniqueConstraints = @UniqueConstraint(name = "uq_variant_option_value", columnNames = {"variant_id", "option_value_id"}))
@Getter
@Setter
public class VariantOptionValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id", nullable = false)
    private ProductOptionValue optionValue;
}
