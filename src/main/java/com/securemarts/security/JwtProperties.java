package com.securemarts.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String accessSecret;
    private String refreshSecret;
    private long accessTtl = 900;
    private long refreshTtl = 604800;
    private String issuer = "securemarts-platform";

    public String getAccessSecret() { return accessSecret; }
    public void setAccessSecret(String accessSecret) { this.accessSecret = accessSecret; }
    public String getRefreshSecret() { return refreshSecret; }
    public void setRefreshSecret(String refreshSecret) { this.refreshSecret = refreshSecret; }
    public long getAccessTtl() { return accessTtl; }
    public void setAccessTtl(long accessTtl) { this.accessTtl = accessTtl; }
    public long getRefreshTtl() { return refreshTtl; }
    public void setRefreshTtl(long refreshTtl) { this.refreshTtl = refreshTtl; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public Duration getAccessTtlDuration() { return Duration.ofSeconds(accessTtl); }
    public Duration getRefreshTtlDuration() { return Duration.ofSeconds(refreshTtl); }
}
