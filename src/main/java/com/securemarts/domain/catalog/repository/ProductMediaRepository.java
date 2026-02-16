package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {

    List<ProductMedia> findByProduct_IdInOrderByPositionAsc(List<Long> productIds);
}
