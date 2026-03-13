package com.securemarts.domain.inventory.controller;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.inventory.dto.*;
import com.securemarts.domain.inventory.service.StockTransferService;
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
@RequestMapping("/stores/{storePublicId}/stock-transfers")
@RequiredArgsConstructor
@Tag(name = "Stock Transfers", description = "Transfer inventory between locations within a store")
@SecurityRequirement(name = "bearerAuth")
public class StockTransferController {

    private final StockTransferService stockTransferService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @PostMapping
    @Operation(summary = "Create stock transfer",
            description = "Creates a new stock transfer in DRAFT status between two locations. "
                    + "Add line items with variant IDs and quantities to transfer.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> create(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID", schema = @Schema(example = "d4e5f6a7-b8c9-0123-def0-456789abcdef"))
            @PathVariable String storePublicId,
            @Valid @RequestBody CreateStockTransferRequest request) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(stockTransferService.create(storeId, request));
    }

    @GetMapping
    @Operation(summary = "List stock transfers",
            description = "Paginated list, optionally filtered by status: DRAFT, PENDING, IN_TRANSIT, PARTIAL, RECEIVED, CANCELLED")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<StockTransferResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Filter by status",
                    schema = @Schema(allowableValues = {"DRAFT", "PENDING", "IN_TRANSIT", "PARTIAL", "RECEIVED", "CANCELLED"}, example = "IN_TRANSIT"))
            @RequestParam(required = false) String status,
            Pageable pageable) {
        ensureAccess(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.list(storeId, status, pageable));
    }

    @GetMapping("/{transferPublicId}")
    @Operation(summary = "Get stock transfer", description = "Returns the transfer with all line items")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Stock transfer public ID", schema = @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            @PathVariable String transferPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.get(storeId, transferPublicId));
    }

    @PutMapping("/{transferPublicId}")
    @Operation(summary = "Update stock transfer", description = "Only allowed while the transfer is in DRAFT status")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> update(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String transferPublicId,
            @Valid @RequestBody UpdateStockTransferRequest request) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.update(storeId, transferPublicId, request));
    }

    @PostMapping("/{transferPublicId}/mark-pending")
    @Operation(summary = "Mark transfer as pending",
            description = "Transitions DRAFT -> PENDING. Reserves stock at origin location "
                    + "and sets quantityIncoming at destination for each line item.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> markPending(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String transferPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.markPending(storeId, transferPublicId));
    }

    @PostMapping("/{transferPublicId}/ship")
    @Operation(summary = "Mark transfer as shipped",
            description = "Transitions PENDING -> IN_TRANSIT. Deducts reserved stock at origin "
                    + "and records TRANSFER_OUT inventory movements.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> ship(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String transferPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.ship(storeId, transferPublicId));
    }

    @PostMapping("/{transferPublicId}/receive")
    @Operation(summary = "Receive transfer items",
            description = "Record received (and optionally rejected) quantities per line item at the destination. "
                    + "Received items are added to available stock at the destination. "
                    + "Status transitions: IN_TRANSIT -> PARTIAL (partial), IN_TRANSIT/PARTIAL -> RECEIVED (all received).")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> receive(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String transferPublicId,
            @Valid @RequestBody ReceiveStockTransferItemsRequest request) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.receive(storeId, transferPublicId, request));
    }

    @PostMapping("/{transferPublicId}/cancel")
    @Operation(summary = "Cancel stock transfer",
            description = "Cancels a transfer. If PENDING: releases reserved stock at origin and clears incoming at destination. "
                    + "If IN_TRANSIT/PARTIAL: clears remaining incoming at destination.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StockTransferResponse> cancel(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String transferPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(stockTransferService.cancel(storeId, transferPublicId));
    }

    @DeleteMapping("/{transferPublicId}")
    @Operation(summary = "Delete draft transfer", description = "Permanently deletes a DRAFT stock transfer")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String transferPublicId) {
        ensureAccess(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        stockTransferService.delete(storeId, transferPublicId);
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
