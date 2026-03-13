package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, Long> {

    List<VariantOptionValue> findByVariantId(Long variantId);

    void deleteByVariantId(Long variantId);
}
