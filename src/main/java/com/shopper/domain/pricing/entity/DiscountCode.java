package com.shopper.domain.pricing.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "discount_codes", indexes = {
        @Index(name = "idx_discount_codes_store_code", columnList = "price_rule_id, code", unique = true)
})
@Getter
@Setter
public class DiscountCode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_rule_id", nullable = false)
    private PriceRule priceRule;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    public boolean isValid() {
        if (usageLimit != null && usageCount >= usageLimit) return false;
        return priceRule != null && priceRule.isActive();
    }
}
