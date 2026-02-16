package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_store_id", columnList = "store_id"),
        @Index(name = "idx_products_collection_id", columnList = "collection_id"),
        @Index(name = "idx_products_status", columnList = "status")
})
@Getter
@Setter
public class Product extends SoftDeletableEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "collection_id")
    private Long collectionId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 255)
    private String handle;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "seo_title", length = 70)
    private String seoTitle;

    @Column(name = "seo_description", length = 320)
    private String seoDescription;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ProductMedia> media = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    public enum ProductStatus {
        DRAFT,
        ACTIVE,
        ARCHIVED
    }
}
