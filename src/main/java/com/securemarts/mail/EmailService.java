package com.securemarts.mail;

import com.securemarts.mail.zeptomail.ZeptomailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Reusable email service for the app. Uses Zeptomail REST API (no SMTP).
 * Configure via APP_MAIL_ZEPTO_API_KEY and APP_MAIL_FROM in .env (see .env.example).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final ZeptomailClient zeptomailClient;

    /**
     * Send a plain-text or HTML email.
     */
    public void send(String to, String subject, String body, boolean html) {
        if (html) {
            zeptomailClient.send(to, null, subject, body, null);
        } else {
            zeptomailClient.send(to, null, subject, null, body);
        }
    }

    /**
     * Send plain-text email.
     */
    public void sendText(String to, String subject, String body) {
        send(to, subject, body, false);
    }

    /**
     * Send HTML email.
     */
    public void sendHtml(String to, String subject, String htmlBody) {
        send(to, subject, htmlBody, true);
    }

    /**
     * Send OTP for email verification. Reusable for any flow (user, rider, etc.).
     */
    public void sendVerificationOtp(String to, String otp, String recipientName) {
        String name = recipientName != null && !recipientName.isBlank() ? recipientName : "there";
        String subject = "Verify your email - Securemarts";
        // Single-line HTML to match Zeptomail working curl payload style.
        String htmlBody = "<div><p>Hi " + escapeHtml(name) + ",</p><p>Your verification code is: <b>" + escapeHtml(otp) + "</b></p><p>This code expires in 10 minutes.</p><p>- Securemarts</p></div>";
        zeptomailClient.send(to, name, subject, htmlBody, null);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Send password reset link/instruction. Use with your token/link.
     */
    public void sendPasswordReset(String to, String resetLink, String recipientName) {
        String name = recipientName != null && !recipientName.isBlank() ? recipientName : "there";
        String subject = "Reset your password - Securemarts";
        String textBody = String.format("""
            Hi %s,

            Use the link below to reset your password:

            %s

            This link expires in 1 hour. If you didn't request this, you can ignore this email.

            - Securemarts
            """, name, resetLink);
        zeptomailClient.send(to, name, subject, null, textBody);
    }
}
