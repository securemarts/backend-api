package com.securemarts.domain.pos.repository;

import com.securemarts.domain.pos.entity.OfflineTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface OfflineTransactionRepository extends JpaRepository<OfflineTransaction, Long> {

    Optional<OfflineTransaction> findByPublicId(String publicId);

    Optional<OfflineTransaction> findByRegisterIdAndClientId(Long registerId, String clientId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM OfflineTransaction t WHERE t.storeId IN :storeIds AND t.syncedAt IS NOT NULL")
    BigDecimal sumAmountByStoreIdInAndSyncedNotNull(@Param("storeIds") Collection<Long> storeIds);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM OfflineTransaction t WHERE t.storeId = :storeId AND t.syncedAt IS NOT NULL AND t.syncedAt >= :from AND t.syncedAt < :to")
    BigDecimal sumAmountByStoreIdAndSyncedAtBetween(@Param("storeId") Long storeId, @Param("from") Instant from, @Param("to") Instant to);
}
