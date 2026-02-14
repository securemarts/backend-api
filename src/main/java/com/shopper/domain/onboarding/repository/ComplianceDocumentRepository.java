package com.shopper.domain.onboarding.repository;

import com.shopper.domain.onboarding.entity.ComplianceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplianceDocumentRepository extends JpaRepository<ComplianceDocument, Long> {

    List<ComplianceDocument> findByBusinessId(Long businessId);
}
