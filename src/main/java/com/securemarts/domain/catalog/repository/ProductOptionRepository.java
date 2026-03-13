package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductIdOrderByPositionAsc(Long productId);
}
