package com.securemarts.domain.order.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.order.dto.OrderResponse;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.order.service.OrderService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storePublicId}/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "List, get, update order lifecycle")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @GetMapping
    @Operation(summary = "List orders", description = "Paginated, optional status filter")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<OrderResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Filter by order status", schema = @Schema(allowableValues = {"PENDING", "CONFIRMED", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})) @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(orderService.listOrders(storeId, status, pageable));
    }

    @GetMapping("/{orderPublicId}")
    @Operation(summary = "Get order")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<OrderResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String orderPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(orderService.getOrder(storeId, orderPublicId));
    }

    @PatchMapping("/{orderPublicId}/status")
    @Operation(summary = "Update order status")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String orderPublicId,
            @RequestBody java.util.Map<String, String> body) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        String status = body != null ? body.get("status") : null;
        if (status == null) {
            throw new IllegalArgumentException("status required");
        }
        Order.OrderStatus s = Order.OrderStatus.valueOf(status);
        return ResponseEntity.ok(orderService.updateStatus(storeId, orderPublicId, s));
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }
}
