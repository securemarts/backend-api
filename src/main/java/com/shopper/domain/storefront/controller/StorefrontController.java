package com.shopper.domain.storefront.controller;

import com.shopper.common.dto.PageResponse;
import com.shopper.domain.catalog.dto.ProductResponse;
import com.shopper.domain.storefront.dto.StorefrontStoreDto;
import com.shopper.domain.storefront.service.StorefrontService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storefront")
@RequiredArgsConstructor
@Tag(name = "Storefront", description = "Public store and catalog for shoppers (no auth required)")
public class StorefrontController {

    private final StorefrontService storefrontService;

    @GetMapping("/{storeSlug}")
    @Operation(summary = "Get store by slug", description = "Returns public store info. Use publicId for cart and checkout. Only active stores.")
    public ResponseEntity<StorefrontStoreDto> getStore(@PathVariable String storeSlug) {
        return ResponseEntity.ok(storefrontService.getStoreBySlug(storeSlug));
    }

    @GetMapping("/{storeSlug}/products")
    @Operation(summary = "List products", description = "Paginated list of active products. Optional search query.")
    public ResponseEntity<PageResponse<ProductResponse>> listProducts(
            @PathVariable String storeSlug,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(storefrontService.listProducts(storeSlug, q, pageable));
    }

    @GetMapping("/{storeSlug}/products/{productPublicId}")
    @Operation(summary = "Get product", description = "Product detail for storefront. Only active products.")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable String storeSlug,
            @PathVariable String productPublicId) {
        return ResponseEntity.ok(storefrontService.getProduct(storeSlug, productPublicId));
    }
}
