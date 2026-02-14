package com.shopper.domain.pricing.repository;

import com.shopper.domain.pricing.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    Optional<DiscountCode> findByPublicId(String publicId);

    Optional<DiscountCode> findByPriceRuleIdAndCode(Long priceRuleId, String code);

    @org.springframework.data.jpa.repository.Query("SELECT dc FROM DiscountCode dc JOIN dc.priceRule pr WHERE LOWER(dc.code) = LOWER(:code) AND pr.storeId = :storeId")
    Optional<DiscountCode> findByCodeAndStoreId(String code, Long storeId);
}
