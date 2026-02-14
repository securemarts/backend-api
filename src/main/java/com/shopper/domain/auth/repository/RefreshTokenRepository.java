package com.shopper.domain.auth.repository;

import com.shopper.domain.auth.entity.RefreshToken;
import com.shopper.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

    void deleteByUser(User user);
}
