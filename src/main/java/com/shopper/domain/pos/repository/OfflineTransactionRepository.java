package com.shopper.domain.pos.repository;

import com.shopper.domain.pos.entity.OfflineTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfflineTransactionRepository extends JpaRepository<OfflineTransaction, Long> {

    Optional<OfflineTransaction> findByPublicId(String publicId);

    Optional<OfflineTransaction> findByRegisterIdAndClientId(Long registerId, String clientId);
}
