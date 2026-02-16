package com.securemarts.domain.logistics.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.logistics.dto.*;
import com.securemarts.domain.logistics.service.LogisticsService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
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
@RequestMapping("/stores/{storePublicId}/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "Store-scoped delivery orders and rider assignment")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final LogisticsService logisticsService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId))
                .getId();
    }

    @PostMapping("/orders")
    @Operation(summary = "Create delivery order for an order")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<DeliveryOrderResponse> createDeliveryOrder(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody CreateDeliveryOrderRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(logisticsService.createDeliveryOrder(storeId, request));
    }

    @GetMapping("/orders")
    @Operation(summary = "List delivery orders for store")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<DeliveryOrderResponse>> listDeliveryOrders(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Filter by delivery status", schema = @Schema(allowableValues = {"PENDING", "ASSIGNED", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "FAILED", "RETURNED"})) @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(logisticsService.listDeliveryOrdersByStore(storeId, status, pageable));
    }

    @GetMapping("/orders/{deliveryOrderPublicId}")
    @Operation(summary = "Get delivery order")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<DeliveryOrderResponse> getDeliveryOrder(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String deliveryOrderPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(logisticsService.getDeliveryOrderByStore(storeId, deliveryOrderPublicId));
    }

    @PatchMapping("/orders/{deliveryOrderPublicId}/assign")
    @Operation(summary = "Assign rider to delivery order")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<DeliveryOrderResponse> assignRider(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String deliveryOrderPublicId,
            @Valid @RequestBody AssignRiderRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(logisticsService.assignRider(storeId, deliveryOrderPublicId, request));
    }

    @PatchMapping("/orders/{deliveryOrderPublicId}/reschedule")
    @Operation(summary = "Reschedule failed/returned delivery for reattempt")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<DeliveryOrderResponse> rescheduleDelivery(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String deliveryOrderPublicId,
            @RequestBody(required = false) RescheduleDeliveryRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        RescheduleDeliveryRequest req = request != null ? request : new RescheduleDeliveryRequest();
        return ResponseEntity.ok(logisticsService.rescheduleDelivery(storeId, deliveryOrderPublicId, req));
    }
}
