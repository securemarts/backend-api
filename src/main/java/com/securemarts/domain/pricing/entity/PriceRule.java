package com.securemarts.domain.pricing.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "price_rules", indexes = {
        @Index(name = "idx_price_rules_store_id", columnList = "store_id")
})
@Getter
@Setter
public class PriceRule extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType;

    @Column(name = "value_amount", precision = 19, scale = 4)
    private BigDecimal valueAmount;

    @Column(name = "value_percent", precision = 5, scale = 2)
    private BigDecimal valuePercent;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @OneToMany(mappedBy = "priceRule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscountCode> discountCodes = new ArrayList<>();

    public enum ValueType {
        FIXED_AMOUNT,
        PERCENTAGE
    }

    public boolean isActive() {
        Instant now = Instant.now();
        if (startsAt != null && now.isBefore(startsAt)) return false;
        if (endsAt != null && now.isAfter(endsAt)) return false;
        if (usageLimit != null && usageCount >= usageLimit) return false;
        return true;
    }
}
