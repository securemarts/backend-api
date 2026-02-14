package com.shopper.domain.admin.controller;

import com.shopper.domain.onboarding.dto.*;
import com.shopper.domain.onboarding.service.MerchantPermissionManagementService;
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
@RequestMapping("/admin/merchant-permissions")
@RequiredArgsConstructor
@Tag(name = "Admin - Merchant RBAC Permissions", description = "CRUD merchant permissions (store-level scopes)")
@SecurityRequirement(name = "bearerAuth")
public class AdminMerchantPermissionController {

    private final MerchantPermissionManagementService merchantPermissionManagementService;

    @GetMapping
    @Operation(summary = "List merchant permissions")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_permission:list')")
    public ResponseEntity<List<MerchantPermissionResponse>> listPermissions() {
        return ResponseEntity.ok(merchantPermissionManagementService.listPermissions());
    }

    @GetMapping("/{permissionPublicId}")
    @Operation(summary = "Get merchant permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_permission:read')")
    public ResponseEntity<MerchantPermissionResponse> getPermission(@PathVariable String permissionPublicId) {
        return ResponseEntity.ok(merchantPermissionManagementService.getPermission(permissionPublicId));
    }

    @PostMapping
    @Operation(summary = "Create merchant permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_permission:create')")
    public ResponseEntity<MerchantPermissionResponse> createPermission(@Valid @RequestBody CreateMerchantPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantPermissionManagementService.createPermission(request));
    }

    @PatchMapping("/{permissionPublicId}")
    @Operation(summary = "Update merchant permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_permission:update')")
    public ResponseEntity<MerchantPermissionResponse> updatePermission(
            @PathVariable String permissionPublicId,
            @Valid @RequestBody UpdateMerchantPermissionRequest request) {
        return ResponseEntity.ok(merchantPermissionManagementService.updatePermission(permissionPublicId, request));
    }

    @DeleteMapping("/{permissionPublicId}")
    @Operation(summary = "Delete merchant permission")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_permission:delete')")
    public ResponseEntity<Void> deletePermission(@PathVariable String permissionPublicId) {
        merchantPermissionManagementService.deletePermission(permissionPublicId);
        return ResponseEntity.noContent().build();
    }
}
