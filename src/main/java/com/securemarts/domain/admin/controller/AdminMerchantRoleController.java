package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.domain.onboarding.dto.*;
import com.securemarts.domain.onboarding.service.MerchantRoleService;
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
@RequestMapping("/admin/merchant-roles")
@RequiredArgsConstructor
@Tag(name = "Admin - Merchant RBAC Roles", description = "CRUD merchant roles, assign permissions to role")
@SecurityRequirement(name = "bearerAuth")
public class AdminMerchantRoleController {

    private final MerchantRoleService merchantRoleService;

    @GetMapping
    @Operation(summary = "List merchant roles")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:list')")
    public ResponseEntity<List<MerchantRoleResponse>> listRoles() {
        return ResponseEntity.ok(merchantRoleService.listRoles());
    }

    @GetMapping("/{rolePublicId}")
    @Operation(summary = "Get merchant role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:read')")
    public ResponseEntity<MerchantRoleResponse> getRole(@PathVariable String rolePublicId) {
        return ResponseEntity.ok(merchantRoleService.getRole(rolePublicId));
    }

    @PostMapping
    @Operation(summary = "Create merchant role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:create')")
    public ResponseEntity<MerchantRoleResponse> createRole(@Valid @RequestBody CreateMerchantRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantRoleService.createRole(request));
    }

    @PatchMapping("/{rolePublicId}")
    @Operation(summary = "Update merchant role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:update')")
    public ResponseEntity<MerchantRoleResponse> updateRole(
            @PathVariable String rolePublicId,
            @Valid @RequestBody UpdateMerchantRoleRequest request) {
        return ResponseEntity.ok(merchantRoleService.updateRole(rolePublicId, request));
    }

    @DeleteMapping("/{rolePublicId}")
    @Operation(summary = "Delete merchant role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:delete')")
    public ResponseEntity<?> deleteRole(@PathVariable String rolePublicId) {
        merchantRoleService.deleteRole(rolePublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{rolePublicId}/permissions")
    @Operation(summary = "List permissions for merchant role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:read')")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable String rolePublicId) {
        return ResponseEntity.ok(merchantRoleService.getPermissionCodesForRole(rolePublicId));
    }

    @PutMapping("/{rolePublicId}/permissions")
    @Operation(summary = "Assign permissions to merchant role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_merchant_role:update')")
    public ResponseEntity<List<String>> assignPermissionsToRole(
            @PathVariable String rolePublicId,
            @Valid @RequestBody AssignMerchantPermissionsToRoleRequest request) {
        return ResponseEntity.ok(merchantRoleService.assignPermissionsToRole(rolePublicId, request));
    }
}
