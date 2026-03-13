package com.securemarts.domain.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class CollectionProductId implements Serializable {

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "product_id", nullable = false)
    private Long productId;
}
