package com.securemarts.domain.storefront.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.catalog.dto.CollectionResponse;
import com.securemarts.domain.catalog.dto.MenuResponse;
import com.securemarts.domain.catalog.dto.ProductResponse;
import com.securemarts.domain.storefront.dto.StorefrontStoreDto;
import com.securemarts.domain.storefront.service.StorefrontService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storefront")
@RequiredArgsConstructor
@Tag(name = "Storefront", description = "Public store and catalog for customers (no auth required)")
public class StorefrontController {

    private final StorefrontService storefrontService;

    @GetMapping("/{storeSlug}")
    @Operation(summary = "Get store by slug", description = "Returns public store info. Use publicId for cart and checkout. Only active stores.")
    public ResponseEntity<StorefrontStoreDto> getStore(
            @Parameter(description = "Store URL slug (e.g. acme-main)", schema = @Schema(example = "acme-main")) @PathVariable String storeSlug) {
        return ResponseEntity.ok(storefrontService.getStoreBySlug(storeSlug));
    }

    @GetMapping("/{storeSlug}/products")
    @Operation(summary = "List products", description = "Paginated list of active products. Optional search query.")
    public ResponseEntity<PageResponse<ProductResponse>> listProducts(
            @Parameter(description = "Store URL slug (e.g. acme-main)", schema = @Schema(example = "acme-main")) @PathVariable String storeSlug,
            @Parameter(description = "Search query (product title, vendor, type)", schema = @Schema(example = "cotton shirt")) @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(storefrontService.listProducts(storeSlug, q, pageable));
    }

    @GetMapping("/{storeSlug}/products/{productPublicId}")
    @Operation(summary = "Get product", description = "Product detail for storefront. Only active products.")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Store URL slug (e.g. acme-main)", schema = @Schema(example = "acme-main")) @PathVariable String storeSlug,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId) {
        return ResponseEntity.ok(storefrontService.getProduct(storeSlug, productPublicId));
    }

    @GetMapping("/{storeSlug}/menus/{handle}")
    @Operation(summary = "Get navigation menu by handle",
            description = "Returns a navigation menu with nested items. "
                    + "Common handles: main-menu, footer. "
                    + "Items can link to collections, products, external URLs, or the store home page.")
    public ResponseEntity<MenuResponse> getMenu(
            @Parameter(description = "Store URL slug (e.g. acme-main)", schema = @Schema(example = "acme-main")) @PathVariable String storeSlug,
            @Parameter(description = "Menu handle (e.g. main-menu, footer)", schema = @Schema(example = "main-menu")) @PathVariable String handle) {
        return ResponseEntity.ok(storefrontService.getMenuByHandle(storeSlug, handle));
    }

    @GetMapping("/{storeSlug}/collections")
    @Operation(summary = "List collections",
            description = "Paginated list of collections for the store. "
                    + "Includes title, handle, image, description, and product count.")
    public ResponseEntity<List<CollectionResponse>> listCollections(
            @Parameter(description = "Store URL slug (e.g. acme-main)", schema = @Schema(example = "acme-main")) @PathVariable String storeSlug) {
        return ResponseEntity.ok(storefrontService.listCollections(storeSlug));
    }

    @GetMapping("/{storeSlug}/collections/{collectionHandle}")
    @Operation(summary = "Get collection by handle",
            description = "Returns collection details including products. "
                    + "Use this to display a collection page on the storefront.")
    public ResponseEntity<CollectionResponse> getCollection(
            @Parameter(description = "Store URL slug (e.g. acme-main)", schema = @Schema(example = "acme-main")) @PathVariable String storeSlug,
            @Parameter(description = "Collection handle (e.g. summer-sale)", schema = @Schema(example = "summer-sale")) @PathVariable String collectionHandle) {
        return ResponseEntity.ok(storefrontService.getCollectionByHandle(storeSlug, collectionHandle));
    }
}
