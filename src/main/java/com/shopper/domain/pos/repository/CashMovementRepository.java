package com.shopper.domain.pos.repository;

import com.shopper.domain.pos.entity.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    List<CashMovement> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
