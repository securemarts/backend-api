package com.securemarts.domain.auth.repository;

import com.securemarts.domain.auth.entity.PasswordResetToken;
import com.securemarts.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);

    void deleteByUser(User user);
}
