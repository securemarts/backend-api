package com.securemarts.domain.logistics.repository;

import com.securemarts.domain.logistics.entity.ProofOfDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, Long> {

    List<ProofOfDelivery> findByDeliveryOrderId(Long deliveryOrderId);
}
