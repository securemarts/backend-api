package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId, Pageable pageable);
}
