package com.securemarts.domain.admin.controller;

import com.securemarts.domain.admin.dto.*;
import com.securemarts.domain.admin.service.AdminAuthService;
import com.securemarts.domain.admin.service.AdminService;
import com.securemarts.domain.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin - Auth", description = "Platform admin: login, create admin, invite admin, complete setup (token + password)")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "Admin login", description = "Authenticate as platform admin. Returns JWT with admin role and scopes (permissions). No refresh token.")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current admin (RBAC)", description = "Returns current admin's identity, roles, and permission scopes for UI.")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('PLATFORM_ADMIN') or hasRole('SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AdminMeResponse> me(@AuthenticationPrincipal String adminPublicId) {
        return ResponseEntity.ok(adminAuthService.getMe(adminPublicId));
    }

    @PostMapping("/admins")
    @Operation(summary = "Create admin", description = "Create admin directly with password (no invite).")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:create')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AdminResponse> createAdmin(
            @AuthenticationPrincipal String adminPublicId,
            @Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(adminPublicId, request));
    }

    @PostMapping("/admins/invite")
    @Operation(summary = "Invite admin", description = "Invite by email; invitee completes setup with token + password via complete-setup.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:invite')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AdminInviteResponse> inviteAdmin(
            @AuthenticationPrincipal String adminPublicId,
            @Valid @RequestBody InviteAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.inviteAdmin(adminPublicId, request));
    }

    @PostMapping("/complete-setup")
    @Operation(summary = "Complete admin setup", description = "Public. Invitee supplies inviteToken, email, and password to create account. No auth required.")
    public ResponseEntity<AdminResponse> completeSetup(@Valid @RequestBody CompleteAdminSetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.completeAdminSetup(request));
    }
}
