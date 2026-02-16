package com.securemarts.domain.logistics.repository;

import com.securemarts.domain.logistics.entity.ServiceZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceZoneRepository extends JpaRepository<ServiceZone, Long> {

    Optional<ServiceZone> findByPublicId(String publicId);

    Page<ServiceZone> findByActiveTrue(Pageable pageable);

    Page<ServiceZone> findByCityAndActiveTrue(String city, Pageable pageable);
}
