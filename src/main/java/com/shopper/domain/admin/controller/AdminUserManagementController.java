package com.shopper.domain.admin.controller;

import com.shopper.common.dto.PageResponse;
import com.shopper.domain.admin.dto.AdminResponse;
import com.shopper.domain.admin.dto.UpdateAdminRequest;
import com.shopper.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "List, get, update, delete platform admins (superuser only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserManagementController {

    private final AdminService adminService;

    @GetMapping("/admins")
    @Operation(summary = "List admins", description = "Paginated list of platform admins.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:list')")
    public ResponseEntity<PageResponse<AdminResponse>> listAdmins(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.listAdmins(pageable));
    }

    @GetMapping("/admins/{adminPublicId}")
    @Operation(summary = "Get admin")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:read')")
    public ResponseEntity<AdminResponse> getAdmin(@PathVariable String adminPublicId) {
        return ResponseEntity.ok(adminService.getAdmin(adminPublicId));
    }

    @PatchMapping("/admins/{adminPublicId}")
    @Operation(summary = "Update admin", description = "Update full name, roles, or active status.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:update')")
    public ResponseEntity<AdminResponse> updateAdmin(
            @AuthenticationPrincipal String requesterAdminPublicId,
            @PathVariable String adminPublicId,
            @Valid @RequestBody UpdateAdminRequest request) {
        return ResponseEntity.ok(adminService.updateAdmin(requesterAdminPublicId, adminPublicId, request));
    }

    @DeleteMapping("/admins/{adminPublicId}")
    @Operation(summary = "Delete admin", description = "Permanently remove admin. Cannot delete yourself.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:delete')")
    public ResponseEntity<Void> deleteAdmin(
            @AuthenticationPrincipal String requesterAdminPublicId,
            @PathVariable String adminPublicId) {
        adminService.deleteAdmin(requesterAdminPublicId, adminPublicId);
        return ResponseEntity.noContent().build();
    }
}
