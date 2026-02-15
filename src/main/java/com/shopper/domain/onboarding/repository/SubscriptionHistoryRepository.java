package com.shopper.domain.onboarding.repository;

import com.shopper.domain.onboarding.entity.SubscriptionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    Page<SubscriptionHistory> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    boolean existsByBusinessIdAndEventType(Long businessId, String eventType);
}
