package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.MerchantPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MerchantPermissionRepository extends JpaRepository<MerchantPermission, Long> {

    Optional<MerchantPermission> findByPublicId(String publicId);

    Optional<MerchantPermission> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = "SELECT DISTINCT p.code FROM merchant_permissions p JOIN merchant_role_permissions mrp ON mrp.permission_id = p.id JOIN merchant_roles mr ON mr.id = mrp.role_id WHERE mr.code = :roleCode", nativeQuery = true)
    List<String> findPermissionCodesByRole(String roleCode);

    @Query(value = "SELECT DISTINCT p.code FROM merchant_permissions p JOIN merchant_role_permissions mrp ON mrp.permission_id = p.id JOIN merchant_roles mr ON mr.id = mrp.role_id WHERE mr.code IN :roleCodes", nativeQuery = true)
    List<String> findPermissionCodesByRoleIn(Set<String> roleCodes);
}
