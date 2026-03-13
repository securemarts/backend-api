package com.securemarts.domain.inventory.controller;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.inventory.dto.*;
import com.securemarts.domain.inventory.service.PurchaseOrderService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.securemarts.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storePublicId}/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Create, manage, and receive purchase orders from suppliers")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @PostMapping
    @Operation(summary = "Create purchase order",
            description = "Creates a new purchase order in DRAFT status. Add line items with variant IDs and quantities.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PurchaseOrderResponse> create(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID", schema = @Schema(example = "d4e5f6a7-b8c9-0123-def0-456789abcdef"))
            @PathVariable String storePublicId,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.create(storeId, request));
    }

    @GetMapping
    @Operation(summary = "List purchase orders",
            description = "Paginated list, optionally filtered by status: DRAFT, ORDERED, PARTIAL, RECEIVED, CANCELLED")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<PurchaseOrderResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Filter by status", schema = @Schema(allowableValues = {"DRAFT", "ORDERED", "PARTIAL", "RECEIVED", "CANCELLED"}, example = "ORDERED"))
            @RequestParam(required = false) String status,
            Pageable pageable) {
        ensureAccess(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(purchaseOrderService.list(storeId, status, pageable));
    }

    @GetMapping("/{poPublicId}")
    @Operation(summary = "Get purchase order", description = "Returns the purchase order with all line items")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PurchaseOrderResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Purchase order public ID", schema = @Schema(example = "f1e2d3c4-b5a6-7890-fedc-ba0987654321"))
            @PathVariable String poPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(purchaseOrderService.get(storeId, poPublicId));
    }

    @PutMapping("/{poPublicId}")
    @Operation(summary = "Update purchase order", description = "Only allowed while the PO is in DRAFT status")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PurchaseOrderResponse> update(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String poPublicId,
            @Valid @RequestBody UpdatePurchaseOrderRequest request) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(purchaseOrderService.update(storeId, poPublicId, request));
    }

    @PostMapping("/{poPublicId}/mark-ordered")
    @Operation(summary = "Mark purchase order as ordered",
            description = "Transitions DRAFT -> ORDERED. Sets orderedAt timestamp and increments quantityIncoming at the destination location for each line item.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PurchaseOrderResponse> markOrdered(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String poPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(purchaseOrderService.markOrdered(storeId, poPublicId));
    }

    @PostMapping("/{poPublicId}/receive")
    @Operation(summary = "Receive purchase order items",
            description = "Record received (and optionally rejected) quantities per line item. "
                    + "Received items are added to available stock at the destination. "
                    + "Status transitions: ORDERED -> PARTIAL (if not all received), ORDERED/PARTIAL -> RECEIVED (if all received).")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PurchaseOrderResponse> receive(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String poPublicId,
            @Valid @RequestBody ReceivePurchaseOrderItemsRequest request) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(purchaseOrderService.receive(storeId, poPublicId, request));
    }

    @PostMapping("/{poPublicId}/cancel")
    @Operation(summary = "Cancel purchase order",
            description = "Cancels a DRAFT, ORDERED, or PARTIAL purchase order. "
                    + "Unreceived incoming quantities are decremented at the destination location.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PurchaseOrderResponse> cancel(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String poPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(purchaseOrderService.cancel(storeId, poPublicId));
    }

    @DeleteMapping("/{poPublicId}")
    @Operation(summary = "Delete draft purchase order", description = "Permanently deletes a DRAFT purchase order")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String poPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        purchaseOrderService.delete(storeId, poPublicId);
        return ResponseEntity.noContent().build();
    }

    private void ensureAccess(String userPublicId, String storePublicId, String permission) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, permission);
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }
}
