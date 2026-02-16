package com.securemarts.domain.inventory.repository;

import com.securemarts.domain.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByPublicIdAndStoreId(String publicId, Long storeId);

    List<Location> findByStoreId(Long storeId);

    long countByStoreId(Long storeId);
}
