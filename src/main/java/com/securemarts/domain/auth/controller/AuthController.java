package com.securemarts.domain.auth.controller;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.domain.auth.dto.*;
import com.securemarts.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify email",
            description = "Confirm email using the 6-digit OTP sent to your email. Request body: email + code (OTP)."
    )
    public ResponseEntity<?> verifyEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email and 6-digit OTP code (not a single token)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = VerifyEmailRequest.class),
                            examples = @ExampleObject(value = "{\"email\":\"user@example.com\",\"code\":\"123456\"}")
                    )
            )
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-email/resend")
    @Operation(summary = "Resend verification OTP", description = "Send a new OTP to the given email. Pass email as query param: ?email=user@example.com")
    public ResponseEntity<?> resendVerifyEmail(@RequestParam(required = false) String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException("Email is required");
        }
        authService.resendVerifyEmail(email.trim());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-phone")
    @Operation(summary = "Verify phone", description = "Confirm phone using OTP (stub). Uses token/code in request body.")
    public ResponseEntity<?> verifyPhone(@Valid @RequestBody VerifyRequest request) {
        authService.verifyPhone(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password/request")
    @Operation(summary = "Request password reset", description = "Send reset link to email")
    public ResponseEntity<?> requestResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.requestResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password/confirm")
    @Operation(summary = "Confirm password reset", description = "Set new password using token from email")
    public ResponseEntity<?> confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request) {
        authService.confirmResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/google")
    @Operation(summary = "Google Sign-In", description = "Verify Google ID token and sign in or create user. Send idToken from client and role (e.g. CUSTOMER). New users get the given role; existing users keep their existing role.")
    public ResponseEntity<TokenResponse> googleSignIn(@Valid @RequestBody GoogleSignInRequest request) {
        return ResponseEntity.ok(authService.googleSignIn(request));
    }
}
