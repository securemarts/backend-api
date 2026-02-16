package com.securemarts.domain.pricing.repository;

import com.securemarts.domain.pricing.entity.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    Optional<PriceRule> findByPublicIdAndStoreId(String publicId, Long storeId);

    List<PriceRule> findByStoreId(Long storeId);

    long countByStoreId(Long storeId);
}
