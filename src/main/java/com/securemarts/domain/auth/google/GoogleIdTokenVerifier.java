package com.securemarts.domain.auth.google;

import com.securemarts.common.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Verifies Google ID tokens via the tokeninfo endpoint and returns user claims.
 * Validates that the token audience (aud) matches the configured Google client ID.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GoogleIdTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=%s";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google.client-id:}")
    private String googleClientId;

    /**
     * Verifies the ID token with Google and returns token info (email, name, etc.).
     * Throws if token is invalid or aud does not match app.google.client-id.
     */
    public GoogleTokenInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BusinessRuleException("ID token is required");
        }
        String url = String.format(TOKENINFO_URL, idToken.trim());
        try {
            ResponseEntity<GoogleTokenInfo> response = restTemplate.getForEntity(URI.create(url), GoogleTokenInfo.class);
            GoogleTokenInfo info = response.getBody();
            if (info == null) {
                throw new BusinessRuleException("Invalid Google ID token");
            }
            if (info.getError() != null) {
                throw new BusinessRuleException("Google token error: " + (info.getErrorDescription() != null ? info.getErrorDescription() : info.getError()));
            }
            if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(info.getAud())) {
                throw new BusinessRuleException("Google ID token audience does not match this application");
            }
            if (info.getEmail() == null || info.getEmail().isBlank()) {
                throw new BusinessRuleException("Google ID token does not contain email");
            }
            return info;
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google token verification failed: {}", e.getMessage());
            throw new BusinessRuleException("Invalid or expired Google ID token");
        }
    }
}
