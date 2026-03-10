package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {

    Optional<BusinessType> findByCode(String code);

    Optional<BusinessType> findByPublicId(String publicId);
}

