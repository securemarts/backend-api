package com.securemarts.domain.admin.repository;

import com.securemarts.domain.admin.entity.PlatformRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlatformRoleRepository extends JpaRepository<PlatformRole, Long> {

    Optional<PlatformRole> findByPublicId(String publicId);

    Optional<PlatformRole> findByCode(String code);

    boolean existsByCode(String code);

    List<PlatformRole> findAllByOrderByCodeAsc();

    List<PlatformRole> findByCodeIn(Set<String> codes);
}
