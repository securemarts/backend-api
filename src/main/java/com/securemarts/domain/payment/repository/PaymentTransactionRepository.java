package com.securemarts.domain.payment.repository;

import com.securemarts.domain.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByPublicId(String publicId);

    Optional<PaymentTransaction> findByGatewayReference(String gatewayReference);

    List<PaymentTransaction> findByOrderId(Long orderId);

    List<PaymentTransaction> findByStoreId(Long storeId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.storeId IN :storeIds AND p.status = 'SUCCESS'")
    BigDecimal sumAmountByStoreIdInAndStatusSuccess(@Param("storeIds") Collection<Long> storeIds);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.storeId = :storeId AND p.status = 'SUCCESS' AND p.createdAt >= :from AND p.createdAt < :to")
    BigDecimal sumAmountByStoreIdAndStatusSuccessAndCreatedAtBetween(@Param("storeId") Long storeId, @Param("from") Instant from, @Param("to") Instant to);
}
