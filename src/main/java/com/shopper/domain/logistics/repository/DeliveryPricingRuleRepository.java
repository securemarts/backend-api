package com.shopper.domain.logistics.repository;

import com.shopper.domain.logistics.entity.DeliveryPricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryPricingRuleRepository extends JpaRepository<DeliveryPricingRule, Long> {

    Optional<DeliveryPricingRule> findByPublicId(String publicId);

    Page<DeliveryPricingRule> findByActiveTrue(Pageable pageable);

    List<DeliveryPricingRule> findByZoneIdAndActiveTrue(Long zoneId);

    List<DeliveryPricingRule> findByOriginHubIdAndDestinationHubIdAndActiveTrue(Long originHubId, Long destinationHubId);
}
