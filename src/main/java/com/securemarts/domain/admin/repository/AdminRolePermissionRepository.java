package com.securemarts.domain.admin.repository;

import com.securemarts.domain.admin.entity.AdminRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminRolePermissionRepository extends JpaRepository<AdminRolePermission, AdminRolePermission.AdminRolePermissionId> {

    List<AdminRolePermission> findByRoleId(Long roleId);

    @Modifying
    @Query("DELETE FROM AdminRolePermission arp WHERE arp.roleId = :roleId")
    void deleteByRoleId(Long roleId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
