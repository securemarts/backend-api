package com.securemarts.domain.order.repository;

import com.securemarts.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPublicId(String publicId);

    Optional<Order> findByStoreIdAndOrderNumber(Long storeId, String orderNumber);

    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    Page<Order> findByStoreIdAndStatus(Long storeId, Order.OrderStatus status, Pageable pageable);

    Long countByStoreId(Long storeId);

    long countByStoreIdIn(Collection<Long> storeIds);

    List<Order> findByStatusAndReservationExpiresAtBefore(Order.OrderStatus status, Instant expiryThreshold);
}
