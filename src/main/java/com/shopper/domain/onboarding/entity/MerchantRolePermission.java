package com.shopper.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "merchant_role_permissions", indexes = {
        @Index(name = "idx_merchant_role_permissions_role_id", columnList = "role_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MerchantRolePermission.MerchantRolePermissionId.class)
public class MerchantRolePermission {

    @Id
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Id
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantRolePermissionId implements Serializable {
        private Long roleId;
        private Long permissionId;
    }
}
