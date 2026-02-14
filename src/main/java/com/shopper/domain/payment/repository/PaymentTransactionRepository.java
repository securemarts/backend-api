package com.shopper.domain.payment.repository;

import com.shopper.domain.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByPublicId(String publicId);

    Optional<PaymentTransaction> findByGatewayReference(String gatewayReference);

    List<PaymentTransaction> findByOrderId(Long orderId);

    List<PaymentTransaction> findByStoreId(Long storeId);
}
