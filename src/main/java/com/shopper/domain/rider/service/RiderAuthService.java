package com.shopper.domain.rider.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.auth.dto.TokenResponse;
import com.shopper.domain.auth.dto.RefreshTokenRequest;
import com.shopper.domain.logistics.entity.Rider;
import com.shopper.domain.logistics.repository.RiderRepository;
import com.shopper.domain.rider.entity.RiderRefreshToken;
import com.shopper.domain.rider.repository.RiderRefreshTokenRepository;
import com.shopper.security.JwtProperties;
import com.shopper.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderAuthService {

    private final RiderRepository riderRepository;
    private final RiderRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public TokenResponse register(com.shopper.domain.rider.dto.RiderRegisterRequest request) {
        if (riderRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && riderRepository.existsByPhone(request.getPhone())) {
            throw new BusinessRuleException("Phone already registered");
        }
        Rider rider = new Rider();
        rider.setEmail(request.getEmail().trim().toLowerCase());
        rider.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        rider.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        rider.setFirstName(request.getFirstName().trim());
        rider.setLastName(request.getLastName().trim());
        rider.setVerificationStatus(Rider.VerificationStatus.PENDING);
        rider.setEmailVerified(false);
        rider = riderRepository.save(rider);
        com.shopper.domain.rider.dto.RiderLoginRequest loginReq = new com.shopper.domain.rider.dto.RiderLoginRequest();
        loginReq.setEmail(request.getEmail());
        loginReq.setPassword(request.getPassword());
        return login(loginReq);
    }

    @Transactional
    public TokenResponse login(com.shopper.domain.rider.dto.RiderLoginRequest request) {
        Rider rider = riderRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), rider.getPasswordHash())) {
            throw new BusinessRuleException("Invalid email or password");
        }
        String accessToken = jwtService.createAccessToken(
                rider.getPublicId(),
                rider.getEmail() != null ? rider.getEmail() : rider.getPhone(),
                List.of("RIDER"),
                List.of(),
                null
        );
        String refreshToken = jwtService.createRefreshToken(rider.getPublicId());
        String jti = jwtService.parseRefreshTokenJti(refreshToken);
        Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshTtlDuration());
        RiderRefreshToken rt = new RiderRefreshToken();
        rt.setRiderId(rider.getId());
        rt.setTokenJti(jti);
        rt.setExpiresAt(expiresAt);
        refreshTokenRepository.save(rt);
        return buildTokenResponse(rider, accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        if (!jwtService.validateRefreshToken(request.getRefreshToken())) {
            throw new BusinessRuleException("Invalid or expired refresh token");
        }
        String subject = jwtService.parseRefreshTokenSubject(request.getRefreshToken());
        String jti = jwtService.parseRefreshTokenJti(request.getRefreshToken());
        Rider rider = riderRepository.findByPublicId(subject)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", subject));
        RiderRefreshToken stored = refreshTokenRepository.findByTokenJti(jti)
                .orElseThrow(() -> new BusinessRuleException("Refresh token not found or already revoked"));
        if (!stored.getRiderId().equals(rider.getId()) || stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BusinessRuleException("Invalid or expired refresh token");
        }
        refreshTokenRepository.delete(stored);
        String accessToken = jwtService.createAccessToken(
                rider.getPublicId(),
                rider.getEmail() != null ? rider.getEmail() : rider.getPhone(),
                List.of("RIDER"),
                List.of(),
                null
        );
        String newRefreshToken = jwtService.createRefreshToken(rider.getPublicId());
        String newJti = jwtService.parseRefreshTokenJti(newRefreshToken);
        Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshTtlDuration());
        RiderRefreshToken rt = new RiderRefreshToken();
        rt.setRiderId(rider.getId());
        rt.setTokenJti(newJti);
        rt.setExpiresAt(expiresAt);
        refreshTokenRepository.save(rt);
        return buildTokenResponse(rider, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        try {
            String jti = jwtService.parseRefreshTokenJti(refreshToken);
            refreshTokenRepository.findByTokenJti(jti).ifPresent(refreshTokenRepository::delete);
        } catch (Exception ignored) {
            // ignore invalid tokens
        }
    }

    private TokenResponse buildTokenResponse(Rider rider, String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTtlDuration().getSeconds())
                .userId(rider.getPublicId())
                .email(rider.getEmail() != null ? rider.getEmail() : rider.getPhone())
                .roles(List.of("RIDER"))
                .scopes(List.of())
                .storeId(null)
                .build();
    }
}
