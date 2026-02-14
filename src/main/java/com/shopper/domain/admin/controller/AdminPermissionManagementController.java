package com.shopper.domain.admin.controller;

import com.shopper.common.dto.ApiResponse;
import com.shopper.domain.admin.dto.*;
import com.shopper.domain.admin.service.AdminPermissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Admin - RBAC Permissions", description = "CRUD admin permissions (scopes)")
@SecurityRequirement(name = "bearerAuth")
public class AdminPermissionManagementController {

    private final AdminPermissionManagementService adminPermissionManagementService;

    @GetMapping
    @Operation(summary = "List permissions", description = "All platform admin permission codes.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_permission:list')")
    public ResponseEntity<List<AdminPermissionResponse>> listPermissions() {
        return ResponseEntity.ok(adminPermissionManagementService.listPermissions());
    }

    @GetMapping("/{permissionPublicId}")
    @Operation(summary = "Get permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_permission:read')")
    public ResponseEntity<AdminPermissionResponse> getPermission(@PathVariable String permissionPublicId) {
        return ResponseEntity.ok(adminPermissionManagementService.getPermission(permissionPublicId));
    }

    @PostMapping
    @Operation(summary = "Create permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_permission:create')")
    public ResponseEntity<AdminPermissionResponse> createPermission(@Valid @RequestBody CreateAdminPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminPermissionManagementService.createPermission(request));
    }

    @PatchMapping("/{permissionPublicId}")
    @Operation(summary = "Update permission", description = "Update description; code is immutable.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_permission:update')")
    public ResponseEntity<AdminPermissionResponse> updatePermission(
            @PathVariable String permissionPublicId,
            @Valid @RequestBody UpdateAdminPermissionRequest request) {
        return ResponseEntity.ok(adminPermissionManagementService.updatePermission(permissionPublicId, request));
    }

    @DeleteMapping("/{permissionPublicId}")
    @Operation(summary = "Delete permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_permission:delete')")
    public ResponseEntity<?> deletePermission(@PathVariable String permissionPublicId) {
        adminPermissionManagementService.deletePermission(permissionPublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
