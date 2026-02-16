package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.ComplianceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplianceDocumentRepository extends JpaRepository<ComplianceDocument, Long> {

    List<ComplianceDocument> findByBusinessId(Long businessId);
}
