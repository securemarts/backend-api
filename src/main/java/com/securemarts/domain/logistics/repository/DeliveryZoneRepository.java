package com.securemarts.domain.logistics.repository;

import com.securemarts.domain.logistics.entity.DeliveryZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {

    Optional<DeliveryZone> findByPublicId(String publicId);

    Page<DeliveryZone> findByActiveTrue(Pageable pageable);

    Page<DeliveryZone> findByHubIdAndActiveTrue(Long hubId, Pageable pageable);
}
