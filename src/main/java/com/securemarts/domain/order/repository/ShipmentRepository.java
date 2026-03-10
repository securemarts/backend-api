package com.securemarts.domain.order.repository;

import com.securemarts.domain.order.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByPublicId(String publicId);

    List<Shipment> findByOrder_IdOrderByCreatedAtAsc(Long orderId);
}
