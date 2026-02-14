package com.shopper.domain.pos.repository;

import com.shopper.domain.pos.entity.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
}
