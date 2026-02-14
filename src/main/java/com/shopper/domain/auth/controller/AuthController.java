package com.shopper.domain.auth.controller;

import com.shopper.domain.auth.dto.*;
import com.shopper.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, login, token refresh, verification, password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user (merchant or customer)")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive access + refresh tokens")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String ua = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(request, ip, ua));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Exchange refresh token for new access and refresh tokens")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke refresh token")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Confirm email using token from link")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-phone")
    @Operation(summary = "Verify phone", description = "Confirm phone using OTP (stub)")
    public ResponseEntity<Void> verifyPhone(@Valid @RequestBody VerifyRequest request) {
        authService.verifyPhone(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/request")
    @Operation(summary = "Request password reset", description = "Send reset link to email")
    public ResponseEntity<Void> requestResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.requestResetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/confirm")
    @Operation(summary = "Confirm password reset", description = "Set new password using token from email")
    public ResponseEntity<Void> confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request) {
        authService.confirmResetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
