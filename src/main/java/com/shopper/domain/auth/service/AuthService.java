package com.shopper.domain.auth.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.domain.auth.dto.*;
import com.shopper.domain.auth.entity.*;
import com.shopper.domain.auth.repository.*;
import com.shopper.mail.EmailService;
import com.shopper.security.JwtProperties;
import com.shopper.security.JwtService;
import com.shopper.security.LoginRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final LoginRateLimiter loginRateLimiter;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    @Value("${app.auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.auth.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Value("${app.auth.password-reset-base-url:}")
    private String passwordResetBaseUrl;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessRuleException("Phone already registered");
        }
        UserType userType = request.getUserType();
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setUserType(userType);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        assignDefaultRole(user, userType);
        user = userRepository.save(user);
        log.info("User registered: {}, sending verification OTP", user.getEmail());
        try {
            emailVerificationService.createAndSendOtp(user.getEmail(), EmailVerificationOtp.TargetType.USER,
                    user.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send verification OTP to {} – user created but email not sent: {}", user.getEmail(), e.getMessage(), e);
            // Don't fail registration; user can use /auth/verify-email/resend
        }
        return buildTokenResponse(user, null);
    }

    private void assignDefaultRole(User user, UserType userType) {
        String roleCode = switch (userType) {
            case MERCHANT_OWNER -> "MERCHANT_OWNER";
            case MERCHANT_STAFF -> "MERCHANT_STAFF";
            case CUSTOMER -> "CUSTOMER";
            case PLATFORM_ADMIN -> "PLATFORM_ADMIN";
            case APP_CLIENT -> "APP_CLIENT";
        };
        roleRepository.findByCode(roleCode).ifPresent(r -> user.getRoles().add(r));
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String key = "login:" + request.getEmail();
        if (!loginRateLimiter.allowRequest(key)) {
            throw new BusinessRuleException("Too many login attempts. Try again later.");
        }
        User user = userRepository.findByEmailWithRolesAndPermissions(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));
        if (user.isLocked()) {
            throw new BusinessRuleException("Account is locked. Try again after " + lockoutDurationMinutes + " minutes.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            incrementFailedAttempts(user);
            throw new BusinessRuleException("Invalid email or password");
        }
        loginRateLimiter.reset(key);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        recordLoginSession(user, ipAddress, userAgent);
        Long storeId = null; // TODO: resolve from business/store when that module is used
        return buildTokenResponse(user, storeId);
    }

    private void incrementFailedAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxLoginAttempts) {
            user.setLocked(true);
            user.setLockedUntil(Instant.now().plusSeconds(lockoutDurationMinutes * 60L));
        }
        userRepository.save(user);
    }

    private void recordLoginSession(User user, String ipAddress, String userAgent) {
        LoginSession session = new LoginSession();
        session.setUser(user);
        session.setLastActiveAt(Instant.now());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setActive(true);
        loginSessionRepository.save(session);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtService.validateRefreshToken(token)) {
            throw new BusinessRuleException("Invalid or expired refresh token");
        }
        String tokenHash = hashToken(token);
        RefreshToken rt = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Refresh token not found or revoked"));
        if (!rt.isValid()) {
            throw new BusinessRuleException("Refresh token expired");
        }
        rt.setRevokedAt(Instant.now());
        refreshTokenRepository.save(rt);
        User user = userRepository.findByEmailWithRolesAndPermissions(
                userRepository.findById(rt.getUser().getId()).orElseThrow().getEmail()).orElseThrow();
        Long storeId = null;
        return buildTokenResponse(user, storeId);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        if (!jwtService.validateRefreshToken(refreshToken)) return;
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .ifPresent(rt -> {
                    rt.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        emailVerificationService.verify(request.getEmail().trim().toLowerCase(), request.getCode(),
                EmailVerificationOtp.TargetType.USER);
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessRuleException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerifyEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessRuleException("No account found for this email"));
        if (user.isEmailVerified()) {
            throw new BusinessRuleException("Email is already verified");
        }
        String recipientName = user.getFirstName() != null ? user.getFirstName() : "";
        emailVerificationService.createAndSendOtp(user.getEmail(), EmailVerificationOtp.TargetType.USER,
                recipientName);
    }

    @Transactional
    public void verifyPhone(VerifyRequest request) {
        // Stub: validate OTP from SMS
        throw new BusinessRuleException("Phone verification not implemented - use verify-email or implement OTP");
    }

    @Transactional
    public void requestResetPassword(ResetPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String rawToken = UUID.randomUUID().toString();
            String tokenHash = hashToken(rawToken);
            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(tokenHash);
            prt.setExpiresAt(Instant.now().plusSeconds(3600));
            passwordResetTokenRepository.save(prt);
            String resetLink = (passwordResetBaseUrl != null && !passwordResetBaseUrl.isBlank())
                    ? passwordResetBaseUrl + (passwordResetBaseUrl.contains("?") ? "&" : "?") + "token=" + rawToken
                    : "Token (use in app within 1 hour): " + rawToken;
            emailService.sendPasswordReset(user.getEmail(), resetLink, user.getFirstName());
            log.info("Password reset email sent to {}", user.getEmail());
        });
    }

    @Transactional
    public void confirmResetPassword(ConfirmResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());
        PasswordResetToken prt = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired reset token"));
        if (!prt.isValid()) {
            throw new BusinessRuleException("Reset token has expired");
        }
        prt.getUser().setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(prt.getUser());
        prt.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(prt);
    }

    private TokenResponse buildTokenResponse(User user, Long storeId) {
        List<String> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toList());
        Set<String> scopes = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream().map(Permission::getCode))
                .collect(Collectors.toSet());
        String accessToken = jwtService.createAccessToken(
                user.getPublicId(),
                user.getEmail(),
                roles,
                List.copyOf(scopes),
                storeId);
        String refreshToken = jwtService.createRefreshToken(user.getPublicId());
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hashToken(refreshToken));
        rt.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTtlDuration()));
        refreshTokenRepository.save(rt);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTtl())
                .userId(user.getPublicId())
                .email(user.getEmail())
                .roles(roles)
                .storeId(storeId)
                .build();
    }

    private static String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
