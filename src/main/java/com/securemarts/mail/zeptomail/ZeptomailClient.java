package com.securemarts.mail.zeptomail;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends email via Zeptomail REST API (no SMTP).
 * Configure APP_MAIL_ZEPTO_API_KEY and APP_MAIL_FROM in .env.
 */
@Component
@Slf4j
public class ZeptomailClient {

    private static final String BASE_URL = "https://api.zeptomail.com/v1.1";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String fromAddress;
    private final String fromName;

    public ZeptomailClient(
            @Value("${app.mail.zeptomail.api-key:}") String apiKey,
            @Value("${app.mail.from:noreply@securemarts.local}") String fromAddress,
            @Value("${app.mail.from-name:Securemarts}") String fromName
    ) {
        this.fromAddress = fromAddress != null ? fromAddress.trim() : "noreply@securemarts.local";
        this.fromName = fromName != null && !fromName.isBlank() ? fromName.trim() : "Securemarts";
        String normalizedKey = normalizeApiKey(apiKey);
        if (normalizedKey == null || normalizedKey.isBlank()) {
            log.warn("APP_MAIL_ZEPTO_API_KEY is not set – verification emails will fail. Set it in .env (Send Mail Token from Zeptomail Agents → SMTP/API).");
        }
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json");
        if (normalizedKey != null && !normalizedKey.isBlank()) {
            builder.defaultHeader("Authorization", "Zoho-enczapikey " + normalizedKey);
        }
        this.restClient = builder.build();
        log.info("Zeptomail From address: {} (APP_MAIL_FROM in .env)", this.fromAddress);
    }

    /** Trim and strip surrounding quotes (e.g. from .env). Use the Send Mail Token from Zeptomail Agents → SMTP/API → Send Mail Token. */
    private static String normalizeApiKey(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    /**
     * Send email. Use either htmlBody or textBody (or both).
     * Builds JSON body explicitly to match Zeptomail API and working curl.
     */
    public void send(String to, String toName, String subject, String htmlBody, String textBody) {
        if (to == null || to.isBlank()) {
            log.warn("Email send skipped: empty recipient");
            return;
        }
        String toAddress = to.trim().toLowerCase();
        String recipientName = toName != null && !toName.isBlank() ? toName : toAddress;

        // Match working Zeptomail sample: from with "address" only (no "name").
        Map<String, String> from = new LinkedHashMap<>();
        from.put("address", fromAddress);

        Map<String, Object> toEntry = new LinkedHashMap<>();
        Map<String, String> emailAddress = new LinkedHashMap<>();
        emailAddress.put("address", toAddress);
        emailAddress.put("name", recipientName);
        toEntry.put("email_address", emailAddress);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", from);
        payload.put("to", List.of(toEntry));
        payload.put("subject", subject != null ? subject : "");
        if (htmlBody != null && !htmlBody.isBlank()) {
            payload.put("htmlbody", htmlBody);
        }
        if (textBody != null && !textBody.isBlank()) {
            payload.put("textbody", textBody);
        }

        String jsonBody;
        try {
            jsonBody = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Zeptomail JSON", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Zeptomail request body: {}", jsonBody);
        }

        try {
            log.info("Sending email to {} (subject: {}), from: {}", to, subject, fromAddress);
            restClient.post()
                    .uri("/email")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(jsonBody)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Zeptomail sent successfully to {}", to);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                log.error("Zeptomail 401 Unauthorized – Invalid API Token. Use the Send Mail Token from Zeptomail: "
                        + "Agents → SMTP/API → Send Mail Token (or API tab). Not the SMTP password. Check APP_MAIL_ZEPTO_API_KEY in .env.");
            }
            String responseBody = e.getResponseBodyAsString();
            log.error("Zeptomail failed for {}: {} – response body: {}", to, e.getMessage(), responseBody != null && !responseBody.isBlank() ? responseBody : "(empty)", e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Zeptomail 500/5xx for {}: {} – response body: [{}]. From: {}. "
                    + "If curl with the same payload works: ensure the Send Mail Token in .env is from the same Zeptomail Agent that has this From address verified. "
                    + "Otherwise contact Zeptomail support with the request body (see DEBUG log).", to, e.getMessage(),
                    responseBody != null && !responseBody.isBlank() ? responseBody : "(empty)", fromAddress, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Zeptomail failed for {}: {} – {}", to, e.getMessage(), e.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
