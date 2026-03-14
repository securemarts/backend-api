package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_items_menu_id", columnList = "menu_id"),
        @Index(name = "idx_menu_items_parent_id", columnList = "parent_id")
})
@Getter
@Setter
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MenuItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<MenuItem> children = new ArrayList<>();

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MenuItemType type;

    @Column(name = "resource_id", length = 36)
    private String resourceId;

    @Column(length = 1000)
    private String url;

    @Column(nullable = false)
    private int position;

    public enum MenuItemType {
        COLLECTION,
        PRODUCT,
        HTTP,
        FRONTPAGE
    }
}
