package com.securemarts.domain.auth.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.domain.auth.entity.EmailVerificationOtp;
import com.securemarts.domain.auth.repository.EmailVerificationOtpRepository;
import com.securemarts.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Creates and verifies email OTPs for users and riders. Reused by auth and rider flows.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int OTP_LENGTH = 6;
    private static final int PASSWORD_RESET_OTP_LENGTH = 5;
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final EmailVerificationOtpRepository otpRepository;
    private final OtpEmailSender otpEmailSender;

    /**
     * Generate OTP, store hash, send email. Replaces any existing OTP for this email+type.
     */
    @Transactional
    public String createAndSendOtp(String email, EmailVerificationOtp.TargetType targetType, String recipientName) {
        String normalizedEmail = email.trim().toLowerCase();
        String otp = generateOtp();
        String otpHash = hashOtp(otp);

        otpRepository.deleteByEmailIgnoreCaseAndTargetType(normalizedEmail, targetType);
        otpRepository.flush();

        EmailVerificationOtp entity = new EmailVerificationOtp();
        entity.setEmail(normalizedEmail);
        entity.setOtpHash(otpHash);
        entity.setTargetType(targetType);
        entity.setExpiresAt(Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60L));
        otpRepository.save(entity);

        log.info("Sending verification OTP to {} (type={})", normalizedEmail, targetType);
        try {
            otpEmailSender.sendOtpEmail(normalizedEmail, otp, recipientName);
            log.info("Verification OTP sent to {} (type={})", normalizedEmail, targetType);
        } catch (Exception e) {
            log.error("Failed to send verification OTP to {} (type={}): {}", normalizedEmail, targetType, e.getMessage(), e);
            // OTP is already saved; user can use resend. Do not propagate so registration tx is not rolled back.
        }
        return otp;
    }

    /**
     * Generate 5-digit OTP for password reset, store hash, send password-reset email. Replaces any existing PASSWORD_RESET OTP for this email.
     */
    @Transactional
    public String createAndSendPasswordResetOtp(String email, String recipientName) {
        String normalizedEmail = email.trim().toLowerCase();
        String otp = generateOtp(PASSWORD_RESET_OTP_LENGTH);
        String otpHash = hashOtp(otp);

        otpRepository.deleteByEmailIgnoreCaseAndTargetType(normalizedEmail, EmailVerificationOtp.TargetType.PASSWORD_RESET);
        otpRepository.flush();

        EmailVerificationOtp entity = new EmailVerificationOtp();
        entity.setEmail(normalizedEmail);
        entity.setOtpHash(otpHash);
        entity.setTargetType(EmailVerificationOtp.TargetType.PASSWORD_RESET);
        entity.setExpiresAt(Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60L));
        otpRepository.save(entity);

        log.info("Sending password reset OTP to {}", normalizedEmail);
        try {
            otpEmailSender.sendPasswordResetOtpEmail(normalizedEmail, otp, recipientName);
            log.info("Password reset OTP sent to {}", normalizedEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP to {}: {}", normalizedEmail, e.getMessage(), e);
        }
        return otp;
    }

    /**
     * Verify code for email+type. Returns true if valid and consumes the OTP.
     */
    @Transactional
    public boolean verify(String email, String code, EmailVerificationOtp.TargetType targetType) {
        String normalizedEmail = email.trim().toLowerCase();
        EmailVerificationOtp otp = otpRepository.findByEmailIgnoreCaseAndTargetType(normalizedEmail, targetType)
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired verification code"));
        if (!otp.isValid()) {
            otpRepository.delete(otp);
            throw new BusinessRuleException("Verification code has expired");
        }
        String codeHash = hashOtp(code);
        if (!codeHash.equals(otp.getOtpHash())) {
            throw new BusinessRuleException("Invalid verification code");
        }
        otpRepository.delete(otp);
        return true;
    }

    private static String generateOtp() {
        return generateOtp(OTP_LENGTH);
    }

    private static String generateOtp(int length) {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    private static String hashOtp(String otp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
