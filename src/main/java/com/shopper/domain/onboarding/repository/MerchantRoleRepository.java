package com.shopper.domain.onboarding.repository;

import com.shopper.domain.onboarding.entity.MerchantRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MerchantRoleRepository extends JpaRepository<MerchantRole, Long> {

    Optional<MerchantRole> findByPublicId(String publicId);

    Optional<MerchantRole> findByCode(String code);

    boolean existsByCode(String code);

    List<MerchantRole> findAllByOrderByCodeAsc();

    List<MerchantRole> findByCodeIn(Set<String> codes);
}
