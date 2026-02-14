package com.shopper.domain.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "admin_role_permissions", indexes = {
        @Index(name = "idx_admin_role_permissions_role_id", columnList = "role_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AdminRolePermission.AdminRolePermissionId.class)
public class AdminRolePermission {

    @Id
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Id
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminRolePermissionId implements Serializable {
        private Long roleId;
        private Long permissionId;
    }
}
