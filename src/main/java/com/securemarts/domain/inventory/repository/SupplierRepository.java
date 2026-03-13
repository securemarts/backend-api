package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByPublicIdAndStoreId(String publicId, Long storeId);

    Page<Supplier> findByStoreIdAndActiveTrue(Long storeId, Pageable pageable);

    Page<Supplier> findByStoreId(Long storeId, Pageable pageable);

    List<Supplier> findByStoreIdAndActiveTrue(Long storeId);
}
