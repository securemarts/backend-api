package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByPublicId(String publicId);

    Optional<InventoryItem> findByStoreIdAndProductVariantId(Long storeId, Long productVariantId);

    List<InventoryItem> findByStoreId(Long storeId);
}
