package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.PurchaseOrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderLineItemRepository extends JpaRepository<PurchaseOrderLineItem, Long> {

    List<PurchaseOrderLineItem> findByPurchaseOrderId(Long purchaseOrderId);
}
