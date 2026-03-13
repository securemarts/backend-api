package com.securemarts.domain.onboarding.controller;

import com.securemarts.domain.onboarding.dto.StoreSettingsResponse;
import com.securemarts.domain.onboarding.dto.UpdateStoreSettingsRequest;
import com.securemarts.domain.onboarding.service.StoreSettingsService;
import com.securemarts.security.CurrentTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storePublicId}/settings")
@RequiredArgsConstructor
@Tag(name = "Store settings", description = "Merchant store settings (e.g. selling channels)")
@SecurityRequirement(name = "bearerAuth")
public class StoreSettingsController {

    private final StoreSettingsService storeSettingsService;

    @GetMapping
    @Operation(summary = "Get store settings")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StoreSettingsResponse> get(@PathVariable String storePublicId) {
        Long storeId = storeSettingsService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(storeSettingsService.get(storePublicId));
    }

    @PatchMapping
    @Operation(summary = "Update store settings", description = "Update selling channel (ONLINE, RETAIL, BOTH, NONE)")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<StoreSettingsResponse> update(
            @PathVariable String storePublicId,
            @Valid @RequestBody UpdateStoreSettingsRequest request) {
        Long storeId = storeSettingsService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(storeSettingsService.update(storePublicId, request));
    }

    private void ensureStoreAccess(Long storeId) {
        Long currentStore = CurrentTenant.getStoreId();
        if (currentStore != null && !currentStore.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("Store context mismatch");
        }
    }
}
