package com.shopper.domain.logistics.repository;

import com.shopper.domain.logistics.entity.DeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    Optional<DeliveryOrder> findByPublicId(String publicId);

    Optional<DeliveryOrder> findByOrderId(Long orderId);

    Page<DeliveryOrder> findByStoreId(Long storeId, Pageable pageable);

    Page<DeliveryOrder> findByStoreIdAndStatus(Long storeId, DeliveryOrder.DeliveryStatus status, Pageable pageable);

    List<DeliveryOrder> findByRiderIdAndStatusIn(Long riderId, List<DeliveryOrder.DeliveryStatus> statuses);

    Page<DeliveryOrder> findByRiderId(Long riderId, Pageable pageable);
}
