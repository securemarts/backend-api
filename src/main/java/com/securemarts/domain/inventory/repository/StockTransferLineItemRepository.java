package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.StockTransferLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransferLineItemRepository extends JpaRepository<StockTransferLineItem, Long> {

    List<StockTransferLineItem> findByStockTransferId(Long stockTransferId);
}
