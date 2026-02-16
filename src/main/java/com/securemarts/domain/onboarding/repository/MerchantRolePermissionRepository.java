package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.MerchantRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MerchantRolePermissionRepository extends JpaRepository<MerchantRolePermission, MerchantRolePermission.MerchantRolePermissionId> {

    List<MerchantRolePermission> findByRoleId(Long roleId);

    @Modifying
    @Query("DELETE FROM MerchantRolePermission mrp WHERE mrp.roleId = :roleId")
    void deleteByRoleId(Long roleId);
}
