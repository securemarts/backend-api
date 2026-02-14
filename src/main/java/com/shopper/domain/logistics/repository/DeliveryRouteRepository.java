package com.shopper.domain.logistics.repository;

import com.shopper.domain.logistics.entity.DeliveryRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {

    Optional<DeliveryRoute> findByPublicId(String publicId);

    Optional<DeliveryRoute> findByOriginHubIdAndDestinationHubId(Long originHubId, Long destinationHubId);

    Page<DeliveryRoute> findByActiveTrue(Pageable pageable);

    Page<DeliveryRoute> findByOriginHubIdAndActiveTrue(Long originHubId, Pageable pageable);
}
