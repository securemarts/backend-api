package com.securemarts.domain.inventory.controller;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.inventory.dto.CreateSupplierRequest;
import com.securemarts.domain.inventory.dto.SupplierResponse;
import com.securemarts.domain.inventory.dto.UpdateSupplierRequest;
import com.securemarts.domain.inventory.service.SupplierService;
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
@RequestMapping("/stores/{storePublicId}/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Manage vendors / suppliers for purchase orders")
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {

    private final SupplierService supplierService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @PostMapping
    @Operation(summary = "Create supplier")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<SupplierResponse> create(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID", schema = @Schema(example = "d4e5f6a7-b8c9-0123-def0-456789abcdef"))
            @PathVariable String storePublicId,
            @Valid @RequestBody CreateSupplierRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(storeId, request));
    }

    @GetMapping
    @Operation(summary = "List suppliers", description = "Paginated list of suppliers. Use activeOnly=true to filter deactivated.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<SupplierResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Only return active suppliers", schema = @Schema(example = "true"))
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(supplierService.list(storeId, activeOnly, pageable));
    }

    @GetMapping("/{supplierPublicId}")
    @Operation(summary = "Get supplier")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<SupplierResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Parameter(description = "Supplier public ID", schema = @Schema(example = "a3b7c9d1-e2f4-5678-abcd-ef1234567890"))
            @PathVariable String supplierPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(supplierService.get(storeId, supplierPublicId));
    }

    @PutMapping("/{supplierPublicId}")
    @Operation(summary = "Update supplier")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<SupplierResponse> update(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String supplierPublicId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(supplierService.update(storeId, supplierPublicId, request));
    }

    @DeleteMapping("/{supplierPublicId}")
    @Operation(summary = "Deactivate supplier", description = "Soft-deletes by setting active = false")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String supplierPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        supplierService.deactivate(storeId, supplierPublicId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }
}
