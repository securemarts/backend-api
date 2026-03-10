package com.securemarts.domain.order.repository;

import com.securemarts.domain.order.entity.OrderItemAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemAllocationRepository extends JpaRepository<OrderItemAllocation, Long> {

    List<OrderItemAllocation> findByOrderItem_OrderId(Long orderId);
}
