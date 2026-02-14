package com.shopper.domain.admin.repository;

import com.shopper.domain.admin.entity.AdminInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AdminInviteRepository extends JpaRepository<AdminInvite, Long> {

    Optional<AdminInvite> findByInviteToken(String inviteToken);

    Optional<AdminInvite> findByInviteTokenAndEmail(String inviteToken, String email);

    boolean existsByEmailAndUsedAtIsNull(String email);

    void deleteByExpiresAtBefore(Instant instant);
}
