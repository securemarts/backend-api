package com.securemarts.domain.pos.controller;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import com.securemarts.domain.pos.dto.*;
import com.securemarts.domain.pos.service.POSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores/{storePublicId}/pos")
@RequiredArgsConstructor
@Tag(name = "POS", description = "Offline-first POS: registers, sessions, sync, cash drawer")
@SecurityRequirement(name = "bearerAuth")
public class POSController {

    private final POSService posService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId))
                .getId();
    }

    @PostMapping("/registers")
    @Operation(summary = "Create POS register")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<POSRegisterResponse> createRegister(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody CreatePOSRegisterRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.createRegister(storeId, request));
    }

    @GetMapping("/registers")
    @Operation(summary = "List POS registers")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<POSRegisterResponse>> listRegisters(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.listRegisters(storeId));
    }

    @GetMapping("/registers/{registerPublicId}")
    @Operation(summary = "Get POS register")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<POSRegisterResponse> getRegister(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.getRegister(storeId, registerPublicId));
    }

    @PostMapping("/registers/{registerPublicId}/sessions/open")
    @Operation(summary = "Open POS session")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<POSSessionResponse> openSession(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId,
            @RequestBody(required = false) OpenSessionRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        OpenSessionRequest req = request != null ? request : new OpenSessionRequest();
        return ResponseEntity.ok(posService.openSession(storeId, registerPublicId, req));
    }

    @PostMapping("/registers/{registerPublicId}/sessions/{sessionPublicId}/close")
    @Operation(summary = "Close POS session")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<POSSessionResponse> closeSession(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId,
            @PathVariable String sessionPublicId,
            @Valid @RequestBody CloseSessionRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.closeSession(storeId, registerPublicId, sessionPublicId, request));
    }

    @GetMapping("/registers/{registerPublicId}/sessions/current")
    @Operation(summary = "Get current open session")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<POSSessionResponse> getCurrentSession(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.getCurrentSession(storeId, registerPublicId));
    }

    @PostMapping("/registers/{registerPublicId}/sync")
    @Operation(summary = "Sync offline transactions", description = "Idempotency-Key header recommended. Returns accepted and conflicts.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<POSSyncResponse> sync(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody POSSyncRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.sync(storeId, registerPublicId, request, idempotencyKey));
    }

    @GetMapping("/registers/{registerPublicId}/cash-drawer")
    @Operation(summary = "Get cash drawer state")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CashDrawerResponse> getCashDrawer(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(posService.getCashDrawer(storeId, registerPublicId));
    }

    @PostMapping("/registers/{registerPublicId}/cash-movements")
    @Operation(summary = "Add cash movement (withdrawal/deposit for reconciliation)")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<?> addCashMovement(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String registerPublicId,
            @Valid @RequestBody CashMovementRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "orders:write");
        Long storeId = resolveStoreId(storePublicId);
        posService.addCashMovement(storeId, registerPublicId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
