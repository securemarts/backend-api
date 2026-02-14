package com.shopper.domain.logistics.repository;

import com.shopper.domain.logistics.entity.DeliveryTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryTrackingEventRepository extends JpaRepository<DeliveryTrackingEvent, Long> {

    List<DeliveryTrackingEvent> findByDeliveryOrderIdOrderByCreatedAtAsc(Long deliveryOrderId);
}
