package com.securemarts.domain.payment.repository;

import com.securemarts.domain.payment.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findByStoreId(Long storeId);
}
