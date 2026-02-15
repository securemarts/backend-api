package com.shopper.domain.rider.controller;

import com.shopper.domain.auth.dto.RefreshTokenRequest;
import com.shopper.domain.auth.dto.TokenResponse;
import com.shopper.common.dto.ApiResponse;
import com.shopper.domain.rider.dto.RiderLoginRequest;
import com.shopper.domain.rider.dto.RiderRegisterRequest;
import com.shopper.domain.rider.service.RiderAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rider/auth")
@RequiredArgsConstructor
@Tag(name = "Rider", description = "Rider auth (register, login, refresh, logout) and KYC (profile, documents)")
public class RiderAuthController {

    private final RiderAuthService riderAuthService;

    @PostMapping("/register")
    @Operation(summary = "Rider register", description = "Self-onboarding. Rider is created with PENDING verification; complete KYC and wait for admin approval to receive deliveries.")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RiderRegisterRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(riderAuthService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Rider login", description = "Authenticate with email + password. Returns access and refresh tokens. Riders are created by admin (no self-registration).")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody RiderLoginRequest request) {
        return ResponseEntity.ok(riderAuthService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Exchange refresh token for new access and refresh tokens")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(riderAuthService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke refresh token")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            riderAuthService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
