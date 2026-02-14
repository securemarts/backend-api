package com.shopper.domain.catalog.repository;

import com.shopper.domain.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByPublicId(String publicId);

    List<ProductVariant> findByProductIdOrderByPosition(Long productId);

    List<ProductVariant> findByPublicIdIn(List<String> publicIds);
}
