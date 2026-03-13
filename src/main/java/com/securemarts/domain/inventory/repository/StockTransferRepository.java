package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    Optional<StockTransfer> findByPublicIdAndStoreId(String publicId, Long storeId);

    Page<StockTransfer> findByStoreId(Long storeId, Pageable pageable);

    Page<StockTransfer> findByStoreIdAndStatus(Long storeId, StockTransfer.StockTransferStatus status, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(st.number, 3) AS int)), 1000) FROM StockTransfer st WHERE st.storeId = :storeId")
    int findMaxSequenceByStoreId(@Param("storeId") Long storeId);
}
