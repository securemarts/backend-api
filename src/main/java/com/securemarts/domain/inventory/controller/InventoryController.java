package com.securemarts.domain.inventory.controller;

import com.securemarts.domain.inventory.dto.CreateInventoryItemRequest;
import com.securemarts.domain.inventory.dto.InventoryAdjustmentRequest;
import com.securemarts.domain.inventory.dto.InventoryItemResponse;
import com.securemarts.domain.inventory.dto.LocationRequest;
import com.securemarts.domain.inventory.entity.Location;
import com.securemarts.domain.inventory.service.InventoryService;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stores/{storePublicId}/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Locations, stock levels, adjust/reserve/release, low stock alerts")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @GetMapping("/locations")
    @Operation(summary = "List locations")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<LocationDto>> listLocations(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        List<Location> list = inventoryService.listLocations(storeId);
        return ResponseEntity.ok(list.stream().map(LocationDto::from).collect(Collectors.toList()));
    }

    @PostMapping("/locations")
    @Operation(summary = "Create location")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<LocationDto> createLocation(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody LocationRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        Location loc = inventoryService.createLocation(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(LocationDto.from(loc));
    }

    @PostMapping("/items")
    @Operation(summary = "Create or get inventory item", description = "Link a product variant (from the business catalog) to a location in this store. Store = merchant's store; location = place within that store (e.g. warehouse, shop). Creates an inventory item with 0 qty if it doesn't exist.")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InventoryItemResponse> createOrGetItem(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody CreateInventoryItemRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(InventoryItemResponse.from(inventoryService.getOrCreateInventoryItem(storeId, request.getVariantPublicId(), request.getLocationPublicId())));
    }

    @GetMapping("/items")
    @Operation(summary = "List inventory items", description = "Per-location inventory for store")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<InventoryItemResponse>> listItems(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(inventoryService.listInventoryByStore(storeId));
    }

    @GetMapping("/items/low-stock")
    @Operation(summary = "Low stock alerts")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<InventoryItemResponse>> lowStock(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:read");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(inventoryService.listLowStock(storeId));
    }

    @PostMapping("/items/{inventoryItemPublicId}/adjust")
    @Operation(summary = "Adjust stock", description = "Positive delta = add, negative = deduct")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InventoryItemResponse> adjust(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String inventoryItemPublicId,
            @Valid @RequestBody InventoryAdjustmentRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(inventoryService.adjustStock(storeId, inventoryItemPublicId, request));
    }

    @PostMapping("/items/{inventoryItemPublicId}/reserve")
    @Operation(summary = "Reserve quantity")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InventoryItemResponse> reserve(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String inventoryItemPublicId,
            @RequestBody Map<String, Integer> body) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        int qty = body != null && body.containsKey("quantity") ? body.get("quantity") : 1;
        return ResponseEntity.ok(inventoryService.reserve(storeId, inventoryItemPublicId, qty));
    }

    @PostMapping("/items/{inventoryItemPublicId}/release")
    @Operation(summary = "Release reserved quantity")
    @PreAuthorize("hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<InventoryItemResponse> release(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @PathVariable String inventoryItemPublicId,
            @RequestBody Map<String, Integer> body) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "inventory:write");
        Long storeId = resolveStoreId(storePublicId);
        int qty = body != null && body.containsKey("quantity") ? body.get("quantity") : 1;
        return ResponseEntity.ok(inventoryService.release(storeId, inventoryItemPublicId, qty));
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }

    @lombok.Data
    @lombok.Builder
    public static class LocationDto {
        private String publicId;
        private String name;
        private String address;
        public static LocationDto from(Location l) {
            return LocationDto.builder().publicId(l.getPublicId()).name(l.getName()).address(l.getAddress()).build();
        }
    }
}
