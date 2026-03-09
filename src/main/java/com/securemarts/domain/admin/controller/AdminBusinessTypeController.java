package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.admin.dto.AdminBusinessTypeRequest;
import com.securemarts.domain.admin.dto.AdminBusinessTypeResponse;
import com.securemarts.domain.admin.service.AdminBusinessTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/business-types")
@RequiredArgsConstructor
@Tag(name = "Admin - Business Types", description = "Manage supported business types for onboarding")
@SecurityRequirement(name = "bearerAuth")
public class AdminBusinessTypeController {

    private final AdminBusinessTypeService adminBusinessTypeService;

    @GetMapping
    @Operation(summary = "List business types", description = "Paginated list of configured business types")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:business-types:read')")
    public ResponseEntity<PageResponse<AdminBusinessTypeResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(adminBusinessTypeService.list(pageable)));
    }

    @PostMapping
    @Operation(summary = "Create business type")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:business-types:write')")
    public ResponseEntity<AdminBusinessTypeResponse> create(@Valid @RequestBody AdminBusinessTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminBusinessTypeService.create(request));
    }

    @PatchMapping("/{publicId}")
    @Operation(summary = "Update business type")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:business-types:write')")
    public ResponseEntity<AdminBusinessTypeResponse> update(
            @PathVariable String publicId,
            @Valid @RequestBody AdminBusinessTypeRequest request) {
        return ResponseEntity.ok(adminBusinessTypeService.update(publicId, request));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Delete business type")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:business-types:write')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        adminBusinessTypeService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}

