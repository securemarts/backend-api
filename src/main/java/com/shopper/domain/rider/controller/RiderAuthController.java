package com.shopper.domain.rider.controller;

import com.shopper.domain.auth.dto.RefreshTokenRequest;
import com.shopper.domain.auth.dto.TokenResponse;
import com.shopper.common.dto.ApiResponse;
import com.shopper.domain.rider.dto.RiderLoginRequest;
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
@Tag(name = "Rider Auth", description = "Rider login, refresh, logout")
public class RiderAuthController {

    private final RiderAuthService riderAuthService;

    @PostMapping("/login")
    @Operation(summary = "Rider login", description = "Authenticate with phone or email + password. Returns access and refresh tokens.")
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
