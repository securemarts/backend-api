package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPublicIdAndStoreId(String publicId, Long storeId);

    Page<PurchaseOrder> findByStoreId(Long storeId, Pageable pageable);

    Page<PurchaseOrder> findByStoreIdAndStatus(Long storeId, PurchaseOrder.PurchaseOrderStatus status, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(po.number, 4) AS int)), 1000) FROM PurchaseOrder po WHERE po.storeId = :storeId")
    int findMaxSequenceByStoreId(@Param("storeId") Long storeId);
}
