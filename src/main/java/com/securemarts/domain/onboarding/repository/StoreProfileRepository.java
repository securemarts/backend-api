package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.StoreProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoreProfileRepository extends JpaRepository<StoreProfile, Long> {

    @Query("SELECT p FROM StoreProfile p LEFT JOIN FETCH p.zone WHERE p.store.id = :storeId")
    Optional<StoreProfile> findByStoreIdWithZone(@Param("storeId") Long storeId);

    @Query("SELECT p.store.id FROM StoreProfile p WHERE p.zone.id = :zoneId")
    List<Long> findStoreIdsByZoneId(@Param("zoneId") Long zoneId);

    List<StoreProfile> findByStore_IdIn(Collection<Long> storeIds);
}
