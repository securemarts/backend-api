package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus", indexes = {
        @Index(name = "idx_menus_store_id", columnList = "store_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_menus_store_handle", columnNames = {"store_id", "handle"})
})
@Getter
@Setter
public class Menu extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 100)
    private String handle;

    @Column(nullable = false, length = 255)
    private String title;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<MenuItem> items = new ArrayList<>();
}
