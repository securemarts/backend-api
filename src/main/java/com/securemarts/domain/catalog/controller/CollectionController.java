package com.securemarts.domain.catalog.controller;

import com.securemarts.domain.catalog.dto.CollectionResponse;
import com.securemarts.domain.catalog.dto.CreateCollectionRequest;
import com.securemarts.domain.catalog.service.CollectionService;
import com.securemarts.security.CurrentTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores/{storePublicId}/collections")
@RequiredArgsConstructor
@Tag(name = "Collections", description = "Create and list collections for a store")
@SecurityRequirement(name = "bearerAuth")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    @Operation(summary = "List collections", description = "List all collections for the business (store context)")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<CollectionResponse>> list(@PathVariable String storePublicId) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        Long businessId = collectionService.resolveBusinessId(storePublicId);
        return ResponseEntity.ok(collectionService.listByBusiness(businessId));
    }

    @GetMapping("/{collectionPublicId}")
    @Operation(summary = "Get collection")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> get(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        Long businessId = collectionService.resolveBusinessId(storePublicId);
        return ResponseEntity.ok(collectionService.get(businessId, collectionPublicId));
    }

    @PostMapping
    @Operation(summary = "Create collection", description = "Create a collection; use its publicId as collectionId when creating products")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> create(
            @PathVariable String storePublicId,
            @Valid @RequestBody CreateCollectionRequest request) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        Long businessId = collectionService.resolveBusinessId(storePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.create(businessId, request));
    }

    private void ensureStoreAccess(Long storeId) {
        Long currentStore = CurrentTenant.getStoreId();
        if (currentStore != null && !currentStore.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("Store context mismatch");
        }
    }
}
