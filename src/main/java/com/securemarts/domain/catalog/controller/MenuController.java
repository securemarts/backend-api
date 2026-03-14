package com.securemarts.domain.catalog.controller;

import com.securemarts.domain.catalog.dto.*;
import com.securemarts.domain.catalog.service.MenuService;
import com.securemarts.security.CurrentTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/stores/{storePublicId}/menus")
@RequiredArgsConstructor
@Tag(name = "Navigation Menus", description = "Manage store navigation menus (main menu, footer, etc.) with nested items linking to collections, products, or external URLs")
@SecurityRequirement(name = "bearerAuth")
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @Operation(summary = "Create menu",
            description = "Create a navigation menu with nested items. "
                    + "Items can link to collections (type=COLLECTION, resourceId=collection publicId), "
                    + "products (type=PRODUCT, resourceId=product publicId), "
                    + "external URLs (type=HTTP, url=https://...), "
                    + "or the store home page (type=FRONTPAGE). "
                    + "Items can be nested up to 3 levels deep via the 'children' field.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<MenuResponse> create(
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Valid @RequestBody CreateMenuRequest request) {
        Long storeId = menuService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(storeId, request));
    }

    @GetMapping
    @Operation(summary = "List menus", description = "List all navigation menus for the store")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<MenuResponse>> list(
            @Parameter(description = "Store public ID") @PathVariable String storePublicId) {
        Long storeId = menuService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(menuService.list(storeId));
    }

    @GetMapping("/{menuPublicId}")
    @Operation(summary = "Get menu", description = "Get a menu with all its nested items")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<MenuResponse> get(
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Menu public ID") @PathVariable String menuPublicId) {
        Long storeId = menuService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(menuService.get(storeId, menuPublicId));
    }

    @PutMapping("/{menuPublicId}")
    @Operation(summary = "Update menu",
            description = "Update a menu's title and/or items. "
                    + "When items are provided, ALL existing items are replaced (full replace). "
                    + "Send the complete items tree including children.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<MenuResponse> update(
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Menu public ID") @PathVariable String menuPublicId,
            @Valid @RequestBody UpdateMenuRequest request) {
        Long storeId = menuService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(menuService.update(storeId, menuPublicId, request));
    }

    @DeleteMapping("/{menuPublicId}")
    @Operation(summary = "Delete menu", description = "Delete a menu and all its items")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Menu public ID") @PathVariable String menuPublicId) {
        Long storeId = menuService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        menuService.delete(storeId, menuPublicId);
        return ResponseEntity.noContent().build();
    }

    private void ensureStoreAccess(Long storeId) {
        Long currentStore = CurrentTenant.getStoreId();
        if (currentStore != null && !currentStore.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("Store context mismatch");
        }
    }
}
