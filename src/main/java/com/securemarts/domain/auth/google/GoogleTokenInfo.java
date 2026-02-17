package com.securemarts.domain.auth.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response from Google tokeninfo endpoint (oauth2.googleapis.com/tokeninfo?id_token=...).
 * Used to verify Google ID token and extract user info.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenInfo {

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    @JsonProperty("name")
    private String name;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    @JsonProperty("aud")
    private String aud;

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("exp")
    private String exp;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public boolean isEmailVerified() {
        return "true".equalsIgnoreCase(emailVerified);
    }
}
