package com.securemarts.domain.pos.repository;

import com.securemarts.domain.pos.entity.POSSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface POSSessionRepository extends JpaRepository<POSSession, Long> {

    Optional<POSSession> findByPublicId(String publicId);

    List<POSSession> findByRegisterIdOrderByOpenedAtDesc(Long registerId, org.springframework.data.domain.Pageable pageable);

    Optional<POSSession> findByRegisterIdAndStatus(Long registerId, POSSession.SessionStatus status);
}
