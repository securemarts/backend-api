package com.shopper.domain.payment.repository;

import com.shopper.domain.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentTransactionId(Long paymentTransactionId);
}
