package com.securemarts.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    public String createAccessToken(String subject, String email, List<String> roles, List<String> scopes, Long storeId) {
        SecretKey key = Keys.hmacShaKeyFor(props.getAccessSecret().getBytes(StandardCharsets.UTF_8));
        Date expiry = Date.from(Instant.now().plus(props.getAccessTtlDuration()));
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .claim("roles", roles)
                .claim("scope", scopes)
                .claim("storeId", storeId)
                .issuer(props.getIssuer())
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(props.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
        Date expiry = Date.from(Instant.now().plus(props.getRefreshTtlDuration()));
        return Jwts.builder()
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .issuer(props.getIssuer())
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public JwtClaims parseAccessToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(props.getAccessSecret().getBytes(StandardCharsets.UTF_8));
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims claims = jws.getPayload();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        @SuppressWarnings("unchecked")
        List<String> scope = claims.get("scope", List.class);
        return JwtClaims.builder()
                .subject(claims.getSubject())
                .email(claims.get("email", String.class))
                .roles(roles != null ? roles : List.of())
                .scopes(scope != null ? scope : List.of())
                .storeId(claims.get("storeId", Long.class))
                .expiration(claims.getExpiration().toInstant())
                .build();
    }

    public String parseRefreshTokenSubject(String token) {
        SecretKey key = Keys.hmacShaKeyFor(props.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return jws.getPayload().getSubject();
    }

    /** Returns the JTI (ID) of the refresh token for revocation/store. */
    public String parseRefreshTokenJti(String token) {
        SecretKey key = Keys.hmacShaKeyFor(props.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return jws.getPayload().getId();
    }

    public boolean validateAccessToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(props.getAccessSecret().getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
            log.debug("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(props.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
            log.debug("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class JwtClaims {
        private String subject;
        private String email;
        private List<String> roles;
        private List<String> scopes;
        private Long storeId;
        private Instant expiration;
    }
}
