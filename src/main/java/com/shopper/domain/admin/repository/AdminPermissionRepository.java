package com.shopper.domain.admin.repository;

import com.shopper.domain.admin.entity.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {

    Optional<AdminPermission> findByPublicId(String publicId);

    Optional<AdminPermission> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = "SELECT DISTINCT p.code FROM admin_permissions p JOIN admin_role_permissions arp ON arp.permission_id = p.id JOIN platform_roles pr ON pr.id = arp.role_id WHERE pr.code IN :roleCodes", nativeQuery = true)
    List<String> findPermissionCodesByRoleIn(Set<String> roleCodes);
}
