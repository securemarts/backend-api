package com.securemarts.domain.onboarding.repository;

import com.securemarts.domain.onboarding.entity.SubscriptionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    Page<SubscriptionHistory> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    boolean existsByBusinessIdAndEventType(Long businessId, String eventType);

    @Query("SELECT h.businessId, MIN(h.createdAt) FROM SubscriptionHistory h WHERE h.businessId IN :businessIds GROUP BY h.businessId")
    List<Object[]> findEarliestCreatedAtByBusinessIdIn(@Param("businessIds") List<Long> businessIds);
}
