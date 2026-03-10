package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.admin.dto.AdminBusinessDetailResponse;
import com.securemarts.domain.admin.dto.AdminBusinessUserSummary;
import com.securemarts.domain.admin.dto.AdminSubscriptionUpdateRequest;
import com.securemarts.domain.admin.dto.BusinessVerificationUpdateRequest;
import com.securemarts.domain.admin.repository.AdminRepository;
import com.securemarts.domain.admin.service.AdminService;
import com.securemarts.domain.audit.entity.AuditLog;
import com.securemarts.domain.audit.service.AuditLogService;
import com.securemarts.domain.onboarding.dto.BusinessResponse;
import com.securemarts.domain.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Business Management", description = "Manage businesses: list, approve/reject verification")
@SecurityRequirement(name = "bearerAuth")
public class AdminBusinessManagementController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;
    private final AdminRepository adminRepository;

    @GetMapping("/businesses")
    @Operation(summary = "List businesses", description = "Paginated. Optional filter by verification status (PENDING, UNDER_REVIEW, APPROVED, REJECTED).")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:list')")
    public ResponseEntity<PageResponse<BusinessResponse>> listBusinesses(
            @Parameter(description = "Filter by verification status", schema = @Schema(allowableValues = {"PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"})) @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.listBusinesses(status, pageable));
    }

    @GetMapping("/businesses/{businessPublicId}")
    @Operation(summary = "Get business detail", description = "Admin business detail by UUID: core info, subscription, store/owner/order counts, and stores list.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:list')")
    public ResponseEntity<AdminBusinessDetailResponse> getBusiness(
            @Parameter(description = "Business public ID (UUID)") @PathVariable String businessPublicId) {
        return ResponseEntity.ok(adminService.getBusinessByPublicId(businessPublicId));
    }

    @GetMapping("/businesses/{businessPublicId}/users")
    @Operation(summary = "List business users", description = "Owners and members associated with this business.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:list')")
    public ResponseEntity<List<AdminBusinessUserSummary>> listBusinessUsers(
            @Parameter(description = "Business public ID (UUID)") @PathVariable String businessPublicId) {
        return ResponseEntity.ok(adminService.listBusinessUsers(businessPublicId));
    }

    @GetMapping("/businesses/{businessPublicId}/orders")
    @Operation(summary = "List business orders", description = "Paginated orders across all stores of this business.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:list')")
    public ResponseEntity<PageResponse<OrderResponse>> listBusinessOrders(
            @Parameter(description = "Business public ID (UUID)") @PathVariable String businessPublicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getOrdersForBusiness(businessPublicId, pageable));
    }

    @PatchMapping("/businesses/{businessPublicId}/verification")
    @Operation(summary = "Approve or reject business", description = "Set verification status to APPROVED or REJECTED. Rejection reason required when rejecting.")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_business:approve')")
    public ResponseEntity<BusinessResponse> updateBusinessVerification(
            @PathVariable String businessPublicId,
            @AuthenticationPrincipal String adminPublicId,
            @Valid @RequestBody BusinessVerificationUpdateRequest request,
            HttpServletRequest httpRequest) {
        BusinessResponse res = adminService.updateBusinessVerification(businessPublicId, adminPublicId, request);
        String label = adminRepository.findByPublicId(adminPublicId).map(a -> a.getFullName() + " (Admin)").orElse("Admin");
        auditLogService.record(AuditLog.ActorType.ADMIN, adminPublicId, label, "Updated verification", "Business", "status=" + request.getStatus(), httpRequest);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/businesses/{businessPublicId}/subscription")
    @Operation(summary = "Update business subscription", description = "Set plan, status, trial end, or period end (for manual trials or support-led upgrades). Admin only.")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('PLATFORM_ADMIN') or hasRole('SUPPORT')")
    public ResponseEntity<BusinessResponse> updateBusinessSubscription(
            @PathVariable String businessPublicId,
            @AuthenticationPrincipal String adminPublicId,
            @Valid @RequestBody AdminSubscriptionUpdateRequest request,
            HttpServletRequest httpRequest) {
        BusinessResponse res = adminService.updateBusinessSubscription(businessPublicId, request);
        String label = adminRepository.findByPublicId(adminPublicId).map(a -> a.getFullName() + " (Admin)").orElse("Admin");
        auditLogService.record(AuditLog.ActorType.ADMIN, adminPublicId, label, "Edited subscription", "Subscription", "plan=" + (request.getPlan() != null ? request.getPlan() : ""), httpRequest);
        return ResponseEntity.ok(res);
    }
}
