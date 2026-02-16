package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByPublicId(String publicId);

    boolean existsByCacNumber(String cacNumber);

    Page<Business> findByVerificationStatus(Business.VerificationStatus status, Pageable pageable);
}
