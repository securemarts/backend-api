package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId, Pageable pageable);

    List<InventoryMovement> findByInventoryItem_StoreIdAndReferenceTypeAndReferenceIdAndMovementType(
            Long storeId, String referenceType, String referenceId, String movementType);
}
