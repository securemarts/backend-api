package com.securemarts.domain.onboarding.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "merchant_permissions", indexes = {
        @Index(name = "idx_merchant_permissions_code", columnList = "code")
})
@Getter
@Setter
public class MerchantPermission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 255)
    private String description;
}
