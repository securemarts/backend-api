package com.securemarts.domain.pos.repository;

import com.securemarts.domain.pos.entity.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
}
