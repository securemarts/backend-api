package com.shopper.domain.admin.controller;

import com.shopper.common.dto.PageResponse;
import com.shopper.domain.admin.dto.AdminSubscriptionUpdateRequest;
import com.shopper.domain.admin.dto.BusinessVerificationUpdateRequest;
import com.shopper.domain.admin.service.AdminService;
import com.shopper.domain.onboarding.dto.BusinessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Admin - Business Management", description = "Manage businesses: list, approve/reject verification")
@SecurityRequirement(name = "bearerAuth")
public class AdminBusinessManagementController {

    private final AdminService adminService;

    @GetMapping("/businesses")
    @Operation(summary = "List businesses", description = "Paginated. Optional filter by verification status (PENDING, UNDER_REVIEW, APPROVED, REJECTED).")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:list')")
    public ResponseEntity<PageResponse<BusinessResponse>> listBusinesses(
            @Parameter(description = "Filter by verification status", schema = @Schema(allowableValues = {"PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"})) @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.listBusinesses(status, pageable));
    }

    @PatchMapping("/businesses/{businessPublicId}/verification")
    @Operation(summary = "Approve or reject business", description = "Set verification status to APPROVED or REJECTED. Rejection reason required when rejecting.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:approve')")
    public ResponseEntity<BusinessResponse> updateBusinessVerification(
            @PathVariable String businessPublicId,
            @AuthenticationPrincipal String adminPublicId,
            @Valid @RequestBody BusinessVerificationUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateBusinessVerification(businessPublicId, adminPublicId, request));
    }

    @PatchMapping("/businesses/{businessPublicId}/subscription")
    @Operation(summary = "Update business subscription", description = "Set plan, status, trial end, or period end (for manual trials or support-led upgrades). Admin only.")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('PLATFORM_ADMIN') or hasRole('SUPPORT')")
    public ResponseEntity<BusinessResponse> updateBusinessSubscription(
            @PathVariable String businessPublicId,
            @Valid @RequestBody AdminSubscriptionUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateBusinessSubscription(businessPublicId, request));
    }
}
