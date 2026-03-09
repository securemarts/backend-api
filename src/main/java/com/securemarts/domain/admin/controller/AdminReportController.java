package com.securemarts.domain.admin.controller;

import com.securemarts.domain.admin.dto.MerchantAnalyticsResponse;
import com.securemarts.domain.admin.dto.StoreSalesSummaryResponse;
import com.securemarts.domain.admin.dto.SubscriptionAnalyticsResponse;
import com.securemarts.domain.admin.service.AdminReportService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin - Reports", description = "Merchant and subscription analytics")
@SecurityRequirement(name = "bearerAuth")
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final StoreRepository storeRepository;

    @GetMapping("/merchant-analytics")
    @Operation(summary = "Merchant analytics", description = "Totals, trend, status distribution, merchant activity table")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:reports:read')")
    public ResponseEntity<MerchantAnalyticsResponse> merchantAnalytics(
            @Parameter(description = "Plan filter") @RequestParam(required = false) String plan,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Period in days (e.g. 30)") @RequestParam(required = false) String period) {
        return ResponseEntity.ok(adminReportService.getMerchantAnalytics(plan, status, period));
    }

    @GetMapping("/subscription-analytics")
    @Operation(summary = "Subscription analytics", description = "Totals, trend, plan distribution, subscription activity")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:reports:read')")
    public ResponseEntity<SubscriptionAnalyticsResponse> subscriptionAnalytics(
            @Parameter(description = "Period in days") @RequestParam(required = false) String period,
            @Parameter(description = "Plan filter") @RequestParam(required = false) String plan,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminReportService.getSubscriptionAnalytics(period, plan, status));
    }

    @GetMapping("/stores/{storeIdOrPublicId}/sales-summary")
    @Operation(summary = "Store sales by channel", description = "Online (e-commerce) and in-store (POS) revenue for a store with optional daily breakdown")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:reports:read')")
    public ResponseEntity<StoreSalesSummaryResponse> storeSalesSummary(
            @PathVariable String storeIdOrPublicId,
            @Parameter(description = "Period in days (e.g. 30)") @RequestParam(required = false) String period) {
        Long storeId = resolveStoreId(storeIdOrPublicId);
        return ResponseEntity.ok(adminReportService.getStoreSalesSummary(storeId, period));
    }

    private Long resolveStoreId(String storeIdOrPublicId) {
        try {
            return Long.parseLong(storeIdOrPublicId);
        } catch (NumberFormatException e) {
            return storeRepository.findByPublicId(storeIdOrPublicId)
                    .map(store -> store.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store", storeIdOrPublicId));
        }
    }
}
