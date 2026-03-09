package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.onboarding.dto.CreatePlanRequest;
import com.securemarts.domain.onboarding.dto.PlanResponse;
import com.securemarts.domain.onboarding.dto.UpdatePlanRequest;
import com.securemarts.domain.admin.service.AdminPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/admin/plans")
@RequiredArgsConstructor
@Tag(name = "Admin - Plans", description = "CRUD subscription plans")
@SecurityRequirement(name = "bearerAuth")
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    @GetMapping
    @Operation(summary = "List plans", description = "Paginated. Optional search by name/code, filter by status.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:plans:read')")
    public ResponseEntity<PageResponse<PlanResponse>> list(
            @Parameter(description = "Search in name or code") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status", schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE"})) @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminPlanService.list(search, status, pageable));
    }

    @GetMapping("/{planPublicId}")
    @Operation(summary = "Get plan details", description = "Plan with features for Plan Details modal")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:plans:read')")
    public ResponseEntity<PlanResponse> get(@PathVariable String planPublicId) {
        return ResponseEntity.ok(adminPlanService.getByPublicId(planPublicId));
    }

    @PostMapping
    @Operation(summary = "Create plan", description = "New plan with optional features. Creates limits row if code is new.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:plans:create')")
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminPlanService.create(request));
    }

    @PatchMapping("/{planPublicId}")
    @Operation(summary = "Update plan")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:plans:update')")
    public ResponseEntity<PlanResponse> update(
            @PathVariable String planPublicId,
            @Valid @RequestBody UpdatePlanRequest request) {
        return ResponseEntity.ok(adminPlanService.update(planPublicId, request));
    }

    @DeleteMapping("/{planPublicId}")
    @Operation(summary = "Delete plan", description = "Fails if plan has active subscribers")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:plans:delete')")
    public ResponseEntity<Void> delete(@PathVariable String planPublicId) {
        adminPlanService.delete(planPublicId);
        return ResponseEntity.noContent().build();
    }
}
