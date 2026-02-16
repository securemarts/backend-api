package com.securemarts.domain.catalog.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "metafields", indexes = {
        @Index(name = "idx_metafields_owner_ns_key", columnList = "owner_type, owner_id, namespace, key_name", unique = true)
})
@Getter
@Setter
public class Metafield extends BaseEntity {

    @Column(name = "owner_type", nullable = false, length = 50)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String namespace;

    @Column(name = "key_name", nullable = false, length = 100)
    private String keyName;

    @Column(name = "value_text", columnDefinition = "TEXT")
    private String valueText;

    @Column(name = "value_type", length = 20)
    private String valueType = "string";
}
