package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.domain.admin.dto.*;
import com.securemarts.domain.admin.service.PlatformRoleService;
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
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin - RBAC Roles", description = "CRUD platform roles, assign permissions to role")
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleManagementController {

    private final PlatformRoleService platformRoleService;

    @GetMapping
    @Operation(summary = "List roles", description = "All platform roles (SUPERUSER, PLATFORM_ADMIN, SUPPORT and any custom).")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:list')")
    public ResponseEntity<List<PlatformRoleResponse>> listRoles() {
        return ResponseEntity.ok(platformRoleService.listRoles());
    }

    @GetMapping("/{rolePublicId}")
    @Operation(summary = "Get role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:read')")
    public ResponseEntity<PlatformRoleResponse> getRole(@PathVariable String rolePublicId) {
        return ResponseEntity.ok(platformRoleService.getRole(rolePublicId));
    }

    @PostMapping
    @Operation(summary = "Create role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:create')")
    public ResponseEntity<PlatformRoleResponse> createRole(@Valid @RequestBody CreatePlatformRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformRoleService.createRole(request));
    }

    @PatchMapping("/{rolePublicId}")
    @Operation(summary = "Update role", description = "Update name and description; code is immutable.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:update')")
    public ResponseEntity<PlatformRoleResponse> updateRole(
            @PathVariable String rolePublicId,
            @Valid @RequestBody UpdatePlatformRoleRequest request) {
        return ResponseEntity.ok(platformRoleService.updateRole(rolePublicId, request));
    }

    @DeleteMapping("/{rolePublicId}")
    @Operation(summary = "Delete role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:delete')")
    public ResponseEntity<?> deleteRole(@PathVariable String rolePublicId) {
        platformRoleService.deleteRole(rolePublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{rolePublicId}/permissions")
    @Operation(summary = "List permissions for role")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:read')")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable String rolePublicId) {
        return ResponseEntity.ok(platformRoleService.getPermissionCodesForRole(rolePublicId));
    }

    @PutMapping("/{rolePublicId}/permissions")
    @Operation(summary = "Assign permissions to role", description = "Replaces all permissions for this role with the given list.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_role:update')")
    public ResponseEntity<List<String>> assignPermissionsToRole(
            @PathVariable String rolePublicId,
            @Valid @RequestBody AssignPermissionsToRoleRequest request) {
        return ResponseEntity.ok(platformRoleService.assignPermissionsToRole(rolePublicId, request));
    }
}
