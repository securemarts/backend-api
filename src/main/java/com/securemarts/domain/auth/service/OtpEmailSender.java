package com.securemarts.domain.auth.service;

import com.securemarts.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends OTP email in its own transaction so a send failure does not mark the caller's transaction rollback-only.
 */
@Component
@RequiredArgsConstructor
public class OtpEmailSender {

    private final EmailService emailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOtpEmail(String email, String otp, String recipientName) {
        emailService.sendVerificationOtp(email, otp, recipientName);
    }
}
