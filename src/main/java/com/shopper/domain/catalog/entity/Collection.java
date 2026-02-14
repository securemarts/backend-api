package com.shopper.domain.catalog.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "collections", indexes = {
        @Index(name = "idx_collections_business_id", columnList = "business_id")
})
@Getter
@Setter
public class Collection extends BaseEntity {

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String handle;

    @Column(columnDefinition = "TEXT")
    private String description;
}
