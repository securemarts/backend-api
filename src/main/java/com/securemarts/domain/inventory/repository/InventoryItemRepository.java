package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByPublicId(String publicId);

    Optional<InventoryItem> findByProductVariantIdAndLocationId(Long productVariantId, Long locationId);

    List<InventoryItem> findByStoreId(Long storeId);

    List<InventoryItem> findByStoreIdAndProductVariant_IdOrderByQuantityAvailableDesc(Long storeId, Long productVariantId);

    @Query("SELECT ii FROM InventoryItem ii WHERE ii.storeId = :storeId AND ii.lowStockThreshold IS NOT NULL AND ii.quantityAvailable <= ii.lowStockThreshold")
    List<InventoryItem> findLowStockByStoreId(Long storeId);

    List<InventoryItem> findByLocation_IdAndProductVariant_IdIn(Long locationId, java.util.List<Long> productVariantIds);

    @Query("SELECT ii FROM InventoryItem ii JOIN FETCH ii.productVariant v JOIN FETCH v.product WHERE ii.location.id = :locationId")
    List<InventoryItem> findByLocationIdWithVariantAndProduct(@Param("locationId") Long locationId);

    @Query("SELECT ii FROM InventoryItem ii JOIN FETCH ii.productVariant v JOIN FETCH v.product WHERE ii.location.id = :locationId AND v.id IN :variantIds")
    List<InventoryItem> findByLocationIdAndVariantIdInWithVariantAndProduct(@Param("locationId") Long locationId, @Param("variantIds") List<Long> variantIds);
}
