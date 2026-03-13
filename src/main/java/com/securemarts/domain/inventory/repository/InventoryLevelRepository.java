package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.InventoryLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, Long> {

    Optional<InventoryLevel> findByPublicId(String publicId);

    Optional<InventoryLevel> findByInventoryItemIdAndLocationId(Long inventoryItemId, Long locationId);

    List<InventoryLevel> findByInventoryItemId(Long inventoryItemId);

    @Query("SELECT il FROM InventoryLevel il JOIN FETCH il.inventoryItem i JOIN FETCH i.productVariant v JOIN FETCH il.location WHERE i.storeId = :storeId ORDER BY i.id, il.location.id")
    List<InventoryLevel> findByInventoryItem_StoreId(@Param("storeId") Long storeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT il FROM InventoryLevel il WHERE il.inventoryItem.storeId = :storeId AND il.inventoryItem.productVariant.id = :variantId AND il.quantityAvailable > 0 ORDER BY il.quantityAvailable DESC")
    List<InventoryLevel> findByStoreIdAndVariantIdForUpdate(@Param("storeId") Long storeId, @Param("variantId") Long variantId);

    @Query("SELECT il FROM InventoryLevel il JOIN FETCH il.inventoryItem i JOIN FETCH i.productVariant v JOIN FETCH v.product WHERE il.location.id = :locationId")
    List<InventoryLevel> findByLocationIdWithVariantAndProduct(@Param("locationId") Long locationId);

    @Query("SELECT il FROM InventoryLevel il JOIN FETCH il.inventoryItem i JOIN FETCH i.productVariant v JOIN FETCH v.product WHERE il.location.id = :locationId AND i.productVariant.id IN :variantIds")
    List<InventoryLevel> findByLocationIdAndVariantIdInWithVariantAndProduct(@Param("locationId") Long locationId, @Param("variantIds") List<Long> variantIds);

    List<InventoryLevel> findByLocation_IdAndInventoryItem_ProductVariant_IdIn(Long locationId, List<Long> variantIds);
}
