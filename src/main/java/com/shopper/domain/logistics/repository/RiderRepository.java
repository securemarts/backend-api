package com.shopper.domain.logistics.repository;

import com.shopper.domain.logistics.entity.Rider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider, Long> {

    Optional<Rider> findByPublicId(String publicId);

    Optional<Rider> findByPhone(String phone);

    Optional<Rider> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Page<Rider> findAll(Pageable pageable);

    Page<Rider> findByStatus(Rider.RiderStatus status, Pageable pageable);

    /** Chowdeck: available riders in zone for nearest-rider dispatch */
    List<Rider> findByZone_IdAndAvailableTrue(Long zoneId);

    /** Available, verified riders in zone (for dispatch) */
    List<Rider> findByZone_IdAndAvailableTrueAndVerificationStatus(Long zoneId, Rider.VerificationStatus verificationStatus);

    Page<Rider> findByZone_Id(Long zoneId, Pageable pageable);
}
