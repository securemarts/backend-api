package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "collection_rules", indexes = {
        @Index(name = "idx_collection_rules_collection_id", columnList = "collection_id")
})
@Getter
@Setter
public class CollectionRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(nullable = false, length = 50)
    private String field;

    @Column(nullable = false, length = 30)
    private String operator;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(nullable = false)
    private int position;
}
