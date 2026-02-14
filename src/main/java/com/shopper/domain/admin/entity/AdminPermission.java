package com.shopper.domain.admin.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_permissions", indexes = {
        @Index(name = "idx_admin_permissions_code", columnList = "code")
})
@Getter
@Setter
public class AdminPermission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 255)
    private String description;
}
