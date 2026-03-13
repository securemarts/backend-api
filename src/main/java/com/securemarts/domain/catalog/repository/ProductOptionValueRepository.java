package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

    List<ProductOptionValue> findByOptionIdOrderByPositionAsc(Long optionId);

    Optional<ProductOptionValue> findByOptionIdAndValue(Long optionId, String value);
}
