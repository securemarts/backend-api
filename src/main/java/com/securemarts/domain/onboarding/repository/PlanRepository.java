package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {

    Optional<Plan> findByPublicId(String publicId);

    Optional<Plan> findByCode(String code);

    List<Plan> findByCodeIn(Collection<String> codes);

    boolean existsByCode(String code);
}
