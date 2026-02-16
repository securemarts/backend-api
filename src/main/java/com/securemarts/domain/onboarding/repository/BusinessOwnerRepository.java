package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, Long> {

    List<BusinessOwner> findByBusinessId(Long businessId);

    Optional<BusinessOwner> findByBusinessIdAndUserId(Long businessId, Long userId);

    List<BusinessOwner> findByUserId(Long userId);
}
