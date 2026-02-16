package com.securemarts.domain.rider.repository;

import com.securemarts.domain.rider.entity.RiderRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RiderRefreshTokenRepository extends JpaRepository<RiderRefreshToken, Long> {

    Optional<RiderRefreshToken> findByTokenJti(String tokenJti);

    void deleteByRiderIdAndTokenJti(Long riderId, String tokenJti);

    void deleteByRiderId(Long riderId);

    void deleteByExpiresAtBefore(Instant instant);
}
