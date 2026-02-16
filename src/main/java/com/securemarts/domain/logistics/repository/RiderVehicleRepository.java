package com.securemarts.domain.logistics.repository;

import com.securemarts.domain.logistics.entity.RiderVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderVehicleRepository extends JpaRepository<RiderVehicle, Long> {

    Optional<RiderVehicle> findByPublicId(String publicId);

    List<RiderVehicle> findByRiderId(Long riderId);
}
