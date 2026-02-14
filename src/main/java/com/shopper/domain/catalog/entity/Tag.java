package com.shopper.domain.catalog.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tags_business_id", columnList = "business_id")
})
@Getter
@Setter
public class Tag extends BaseEntity {

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 100)
    private String name;
}
