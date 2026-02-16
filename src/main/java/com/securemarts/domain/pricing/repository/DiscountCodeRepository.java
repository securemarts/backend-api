package com.securemarts.domain.pricing.repository;

import com.securemarts.domain.pricing.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    Optional<DiscountCode> findByPublicId(String publicId);

    Optional<DiscountCode> findByPriceRuleIdAndCode(Long priceRuleId, String code);

    @org.springframework.data.jpa.repository.Query("SELECT dc FROM DiscountCode dc JOIN dc.priceRule pr WHERE LOWER(dc.code) = LOWER(:code) AND pr.storeId = :storeId")
    Optional<DiscountCode> findByCodeAndStoreId(String code, Long storeId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(dc) FROM DiscountCode dc JOIN dc.priceRule pr WHERE pr.storeId = :storeId")
    long countByStoreId(@org.springframework.data.repository.query.Param("storeId") Long storeId);
}
