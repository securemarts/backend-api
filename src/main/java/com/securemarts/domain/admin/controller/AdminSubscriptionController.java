package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.admin.dto.AdminSubscriptionUpdateRequest;
import com.securemarts.domain.admin.dto.SubscriptionDetailResponse;
import com.securemarts.domain.admin.dto.SubscriptionListResponse;
import com.securemarts.domain.admin.service.AdminSubscriptionService;
import com.securemarts.domain.onboarding.dto.BusinessResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Admin - Subscriptions", description = "List, view, and update business subscriptions")
@SecurityRequirement(name = "bearerAuth")
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    @GetMapping
    @Operation(summary = "List subscriptions", description = "Paginated. Filter by plan, status, search merchant name.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:subscriptions:read')")
    public ResponseEntity<PageResponse<SubscriptionListResponse>> list(
            @Parameter(description = "Filter by plan code") @RequestParam(required = false) String plan,
            @Parameter(description = "Filter by status", schema = @Schema(allowableValues = {"NONE", "TRIALING", "ACTIVE", "PAST_DUE", "CANCELLED"})) @RequestParam(required = false) String status,
            @Parameter(description = "Search merchant name") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminSubscriptionService.list(plan, status, search, pageable));
    }

    @GetMapping("/{businessPublicId}")
    @Operation(summary = "Get subscription detail", description = "Overview and features for Subscription Details modal")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:subscriptions:read')")
    public ResponseEntity<SubscriptionDetailResponse> get(@PathVariable String businessPublicId) {
        return ResponseEntity.ok(adminSubscriptionService.getByBusinessPublicId(businessPublicId));
    }

    @PatchMapping("/{businessPublicId}")
    @Operation(summary = "Update subscription", description = "Plan, status, period dates")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:subscriptions:update')")
    public ResponseEntity<BusinessResponse> update(
            @PathVariable String businessPublicId,
            @Valid @RequestBody AdminSubscriptionUpdateRequest request) {
        return ResponseEntity.ok(adminSubscriptionService.updateSubscription(businessPublicId, request));
    }
}
