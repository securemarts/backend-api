package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections", indexes = {
        @Index(name = "idx_collections_store_id", columnList = "store_id")
})
@Getter
@Setter
public class Collection extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String handle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Convert(converter = CollectionTypeConverter.class)
    @Column(name = "collection_type", nullable = false, length = 20)
    private CollectionType collectionType = CollectionType.MANUAL;

    @Column(name = "conditions_operator", length = 10)
    private String conditionsOperator;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<CollectionRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<CollectionProduct> collectionProducts = new ArrayList<>();

    public enum CollectionType {
        MANUAL,
        SMART
    }
}
