package com.securemarts.domain.catalog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.dto.ApiResponse;
import com.securemarts.domain.catalog.dto.ProductRequest;
import com.securemarts.domain.catalog.dto.ProductResponse;
import com.securemarts.domain.catalog.service.CatalogService;
import com.securemarts.domain.catalog.service.FileStorageService;
import com.securemarts.domain.onboarding.service.MerchantPermissionService;
import com.securemarts.domain.onboarding.service.StoreAccessService;
import com.securemarts.security.CurrentTenant;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/stores/{storePublicId}/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "CRUD products, variants, media, collections, tags. Products belong to a store; stores have locations that track inventory.")
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

    private final CatalogService catalogService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final StoreAccessService storeAccessService;
    private final MerchantPermissionService merchantPermissionService;

    @GetMapping
    @Operation(summary = "List products", description = "Paginated list with optional status and search (store catalog)")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (products belong to this store)") @PathVariable String storePublicId,
            @Parameter(description = "Filter by product status", schema = @Schema(allowableValues = {"DRAFT", "ACTIVE", "ARCHIVED"})) @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:read");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.listProducts(storeId, status, q, pageable));
    }

    @GetMapping("/{productPublicId}")
    @Operation(summary = "Get product")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> get(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (products belong to this store)") @PathVariable String storePublicId,
            @PathVariable String productPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:read");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.getProduct(storeId, productPublicId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create product (JSON)")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> createJson(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (product is created for this store)") @PathVariable String storePublicId,
            @Valid @RequestBody ProductRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createProduct(storeId, request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create product (form with image upload)", description = "Product is created for this store. Use multipart form: title (required), handle, bodyHtml, status, seoTitle, seoDescription, collectionId, tagNames (comma-separated), variants (JSON string); upload image files as 'media' parts.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> createWithMedia(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (product is created for this store)") @PathVariable String storePublicId,
            @RequestParam String title,
            @RequestParam(required = false) String handle,
            @RequestParam(required = false) String bodyHtml,
            @Parameter(description = "Product status", schema = @Schema(allowableValues = {"DRAFT", "ACTIVE", "ARCHIVED"})) @RequestParam(required = false) String status,
            @RequestParam(required = false) String seoTitle,
            @RequestParam(required = false) String seoDescription,
            @RequestParam(required = false) String collectionId,
            @RequestParam(required = false) String tagNames,
            @RequestParam(required = false) String variants,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles) throws Exception {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);

        ProductRequest request = new ProductRequest();
        request.setTitle(title);
        request.setHandle(handle);
        request.setBodyHtml(bodyHtml);
        request.setStatus(status != null && !status.isBlank() ? status : "DRAFT");
        request.setSeoTitle(seoTitle);
        request.setSeoDescription(seoDescription);
        request.setCollectionId(collectionId == null || collectionId.isBlank() ? null : collectionId);
        if (tagNames != null && !tagNames.isBlank()) {
            request.setTagNames(Stream.of(tagNames.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));
        }
        if (variants != null && !variants.isBlank()) {
            request.setVariants(objectMapper.readValue(variants, new TypeReference<List<ProductRequest.ProductVariantRequest>>() {}));
        }
        List<ProductRequest.ProductMediaRequest> mediaList = new ArrayList<>();
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (int i = 0; i < mediaFiles.size(); i++) {
                MultipartFile f = mediaFiles.get(i);
                if (f == null || f.isEmpty()) continue;
                String url = fileStorageService.store(storePublicId, f);
                if (url != null) {
                    ProductRequest.ProductMediaRequest mr = new ProductRequest.ProductMediaRequest();
                    mr.setUrl(url);
                    mr.setAlt("");
                    mr.setPosition(i);
                    mr.setMediaType("image");
                    mediaList.add(mr);
                }
            }
        }
        request.setMedia(mediaList);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createProduct(storeId, request));
    }

    @PutMapping("/{productPublicId}")
    @Operation(summary = "Update product")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> update(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (products belong to this store)") @PathVariable String storePublicId,
            @PathVariable String productPublicId,
            @Valid @RequestBody ProductRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.updateProduct(storeId, productPublicId, request));
    }

    @DeleteMapping("/{productPublicId}")
    @Operation(summary = "Delete product (soft delete)")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (products belong to this store)") @PathVariable String storePublicId,
            @PathVariable String productPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        catalogService.deleteProduct(storeId, productPublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void ensureStoreAccess(Long storeId) {
        Long currentStore = CurrentTenant.getStoreId();
        if (currentStore != null && !currentStore.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("Store context mismatch");
        }
    }
}
