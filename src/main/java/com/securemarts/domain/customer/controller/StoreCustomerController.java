package com.securemarts.domain.customer.controller;

import com.securemarts.common.dto.ApiResponse;
import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.customer.dto.CreateStoreCustomerRequest;
import com.securemarts.domain.customer.dto.StoreCustomerResponse;
import com.securemarts.domain.customer.dto.UpdateStoreCustomerRequest;
import com.securemarts.domain.customer.service.StoreCustomerService;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/stores/{storePublicId}/customers")
@RequiredArgsConstructor
@Tag(name = "Store customers", description = "Merchant-managed customers for invoicing and credit sales")
@SecurityRequirement(name = "bearerAuth")
public class StoreCustomerController {

    private final StoreCustomerService storeCustomerService;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;
    private final StoreRepository storeRepository;

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }

    @GetMapping
    @Operation(summary = "List customers", description = "Paginated list, optional search by name or phone")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<StoreCustomerResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(PageResponse.of(storeCustomerService.list(storeId, search, pageable)));
    }

    @PostMapping
    @Operation(summary = "Create customer")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StoreCustomerResponse> create(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Valid @RequestBody CreateStoreCustomerRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(storeCustomerService.create(storeId, request));
    }

    @GetMapping("/{customerPublicId}")
    @Operation(summary = "Get customer")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StoreCustomerResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Customer public ID") @PathVariable String customerPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(storeCustomerService.get(storeId, customerPublicId));
    }

    @PatchMapping("/{customerPublicId}")
    @Operation(summary = "Update customer")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StoreCustomerResponse> update(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Customer public ID") @PathVariable String customerPublicId,
            @RequestBody UpdateStoreCustomerRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(storeCustomerService.update(storeId, customerPublicId, request));
    }

    @DeleteMapping("/{customerPublicId}")
    @Operation(summary = "Delete customer")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Customer public ID") @PathVariable String customerPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "customers:write");
        Long storeId = resolveStoreId(storePublicId);
        storeCustomerService.delete(storeId, customerPublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
