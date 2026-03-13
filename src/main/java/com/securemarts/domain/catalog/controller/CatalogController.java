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
    @Operation(summary = "Create product (form with image upload)",
            description = "Create a product for this store using multipart form data. "
                    + "Upload product images as 'media' file parts. "
                    + "Pass options and variants as JSON strings (see parameter descriptions for structure).")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> createWithMedia(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID (product is created for this store)")
            @PathVariable String storePublicId,
            @Parameter(description = "Product title (required)", schema = @Schema(example = "Classic Cotton T-Shirt"))
            @RequestParam String title,
            @Parameter(description = "URL-friendly handle (auto-generated from title if blank)", schema = @Schema(example = "classic-cotton-t-shirt"))
            @RequestParam(required = false) String handle,
            @Parameter(description = "Product description (supports HTML)", schema = @Schema(example = "<p>Soft cotton t-shirt available in multiple sizes.</p>"))
            @RequestParam(required = false) String bodyHtml,
            @Parameter(description = "Product status", schema = @Schema(allowableValues = {"DRAFT", "ACTIVE", "ARCHIVED"}, example = "DRAFT"))
            @RequestParam(required = false) String status,
            @Parameter(description = "Vendor or brand name", schema = @Schema(example = "Nike"))
            @RequestParam(required = false) String vendor,
            @Parameter(description = "Product type/category", schema = @Schema(example = "T-Shirts"))
            @RequestParam(required = false) String productType,
            @Parameter(description = "SEO title (max 70 chars)", schema = @Schema(example = "Classic Cotton T-Shirt | MyStore"))
            @RequestParam(required = false) String seoTitle,
            @Parameter(description = "SEO meta description (max 320 chars)", schema = @Schema(example = "Shop our classic cotton t-shirt in various sizes and colors."))
            @RequestParam(required = false) String seoDescription,
            @Parameter(description = "Collection public IDs (comma-separated). "
                    + "Example: f9ac848c-af80-4b7d-a21f-8ba647ac1566,c728cdd4-ecd8-4490-aeef-2bd1098ffddb",
                    schema = @Schema(example = "f9ac848c-af80-4b7d-a21f-8ba647ac1566,c728cdd4-ecd8-4490-aeef-2bd1098ffddb"))
            @RequestParam(required = false) String collectionIds,
            @Parameter(description = "Tag names (comma-separated). "
                    + "Example: vintage,summer,sale",
                    schema = @Schema(example = "vintage,summer,sale"))
            @RequestParam(required = false) String tagNames,
            @Parameter(description = "Product options as a JSON array. Each object has: "
                    + "name (option name like Size, Color) and values (array of option values). "
                    + "Example: [{\"name\":\"Size\",\"values\":[\"S\",\"M\",\"L\"]},{\"name\":\"Color\",\"values\":[\"Black\",\"White\"]}]",
                    schema = @Schema(example = "[{\"name\":\"Size\",\"values\":[\"S\",\"M\",\"L\"]},{\"name\":\"Color\",\"values\":[\"Black\",\"White\"]}]"))
            @RequestParam(required = false) String options,
            @Parameter(description = "Variants as a JSON array. Each object has: "
                    + "sku (string), title (string), priceAmount (number, required), compareAtAmount (number, original/compare-at price), "
                    + "currency (string, default NGN), barcode (string), weight (number), weightUnit (string, e.g. kg, lb, g), "
                    + "trackInventory (boolean, default true), requiresShipping (boolean, default true), "
                    + "options (object mapping option name to value, e.g. {\"Size\":\"M\",\"Color\":\"Black\"}), "
                    + "inventory (array of {locationId, quantity}), position (number). "
                    + "Example: [{\"sku\":\"TSH-M-BLK\",\"title\":\"Medium / Black\",\"priceAmount\":2999.00,"
                    + "\"compareAtAmount\":3999.00,\"currency\":\"NGN\",\"barcode\":\"1234567890123\","
                    + "\"weight\":0.3,\"weightUnit\":\"kg\",\"trackInventory\":true,\"requiresShipping\":true,"
                    + "\"options\":{\"Size\":\"M\",\"Color\":\"Black\"},"
                    + "\"inventory\":[{\"locationId\":\"e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab\",\"quantity\":50}],\"position\":0}]",
                    schema = @Schema(example = "[{\"sku\":\"TSH-M-BLK\",\"title\":\"Medium / Black\",\"priceAmount\":2999.00,"
                    + "\"compareAtAmount\":3999.00,\"currency\":\"NGN\",\"barcode\":\"1234567890123\","
                    + "\"weight\":0.3,\"weightUnit\":\"kg\",\"trackInventory\":true,\"requiresShipping\":true,"
                    + "\"options\":{\"Size\":\"M\",\"Color\":\"Black\"},"
                    + "\"inventory\":[{\"locationId\":\"e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab\",\"quantity\":50}],\"position\":0}]"))
            @RequestParam(required = false) String variants,
            @Parameter(description = "Product image files to upload")
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
        request.setVendor(vendor);
        request.setProductType(productType);
        request.setSeoTitle(seoTitle);
        request.setSeoDescription(seoDescription);
        if (collectionIds != null && !collectionIds.isBlank()) {
            request.setCollectionIds(Stream.of(collectionIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }
        if (tagNames != null && !tagNames.isBlank()) {
            request.setTagNames(Stream.of(tagNames.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));
        }
        if (options != null && !options.isBlank()) {
            request.setOptions(objectMapper.readValue(options, new TypeReference<List<com.securemarts.domain.catalog.dto.ProductOptionRequest>>() {}));
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

    @PostMapping(value = "/{productPublicId}/variants/{variantPublicId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Attach media to variant", description = "Upload one or more images; they are stored in DigitalOcean Spaces and attached to this variant (e.g. color/size images). Use multipart form with part name 'media' (image files).")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> attachMediaToVariant(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId,
            @Parameter(description = "Variant public ID") @PathVariable String variantPublicId,
            @Parameter(description = "Image files to upload to Spaces and attach to the variant") @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles) throws java.io.IOException {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.attachMediaToVariant(storeId, productPublicId, variantPublicId, mediaFiles));
    }

    @DeleteMapping("/{productPublicId}/variants/{variantPublicId}/media/{mediaPublicId}")
    @Operation(summary = "Detach media from variant", description = "Detach a single media item from a variant. Does not delete the media itself.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> detachMediaFromVariant(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId,
            @Parameter(description = "Variant public ID") @PathVariable String variantPublicId,
            @Parameter(description = "Media public ID") @PathVariable String mediaPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.detachMediaFromVariant(storeId, productPublicId, variantPublicId, mediaPublicId));
    }

    @PostMapping(value = "/{productPublicId}/variants", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add variant (JSON)", description = "Add a variant with JSON body only (no media). For variant + images use the multipart endpoint.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> addVariantJson(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId,
            @Valid @RequestBody ProductRequest.ProductVariantRequest request) throws java.io.IOException {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addVariant(storeId, productPublicId, request));
    }

    @PostMapping(value = "/{productPublicId}/variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Add variant with media",
            description = "Add a variant to a product and optionally upload images. "
                    + "Pass options and inventory as JSON strings (see parameter descriptions for structure)."
    )
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> addVariantWithMedia(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId,
            @Parameter(description = "SKU code", schema = @Schema(example = "TSH-M-BLK"))
            @RequestPart(required = false) String sku,
            @Parameter(description = "Variant title", schema = @Schema(example = "Medium / Black"))
            @RequestPart(required = false) String title,
            @Parameter(description = "Price amount (required)", schema = @Schema(example = "2999.00"))
            @RequestPart java.math.BigDecimal priceAmount,
            @Parameter(description = "Compare-at (original) price", schema = @Schema(example = "3999.00"))
            @RequestPart(required = false) java.math.BigDecimal compareAtAmount,
            @Parameter(description = "Currency code (default NGN)", schema = @Schema(example = "NGN"))
            @RequestPart(required = false) String currency,
            @Parameter(description = "Barcode", schema = @Schema(example = "1234567890123"))
            @RequestPart(required = false) String barcode,
            @Parameter(description = "Weight value", schema = @Schema(example = "0.3"))
            @RequestPart(required = false) java.math.BigDecimal weight,
            @Parameter(description = "Weight unit (kg, lb, g, oz)", schema = @Schema(example = "kg"))
            @RequestPart(required = false) String weightUnit,
            @Parameter(description = "Option name-to-value mapping as a JSON object. "
                    + "Example: {\"Size\":\"M\",\"Color\":\"Black\"}",
                    schema = @Schema(example = "{\"Size\":\"M\",\"Color\":\"Black\"}"))
            @RequestPart(required = false) String options,
            @Parameter(description = "Inventory quantities per location as a JSON array. Each object has: "
                    + "locationId (location public ID) and quantity (integer). "
                    + "Example: [{\"locationId\":\"e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab\",\"quantity\":50}]",
                    schema = @Schema(example = "[{\"locationId\":\"e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab\",\"quantity\":50}]"))
            @RequestPart(required = false) String inventory,
            @Parameter(description = "Display order (0-based)", schema = @Schema(example = "0"))
            @RequestPart(required = false) Integer position,
            @Parameter(description = "Variant image files to upload")
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles) throws java.io.IOException {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        ProductRequest.ProductVariantRequest request = new ProductRequest.ProductVariantRequest();
        request.setSku(sku);
        request.setTitle(title);
        request.setPriceAmount(priceAmount);
        request.setCompareAtAmount(compareAtAmount);
        request.setCurrency(currency != null && !currency.isBlank() ? currency : "NGN");
        request.setBarcode(barcode);
        request.setWeight(weight);
        request.setWeightUnit(weightUnit);
        if (options != null && !options.isBlank()) {
            request.setOptions(objectMapper.readValue(options, new TypeReference<java.util.Map<String, String>>() {}));
        }
        if (inventory != null && !inventory.isBlank()) {
            request.setInventory(objectMapper.readValue(inventory, new TypeReference<List<com.securemarts.domain.catalog.dto.VariantInventoryRequest>>() {}));
        }
        if (position != null) {
            request.setPosition(position);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.addVariantWithMedia(storeId, productPublicId, request, mediaFiles));
    }

    @PutMapping("/{productPublicId}/variants/{variantPublicId}")
    @Operation(summary = "Update product variant")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> updateVariant(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId,
            @Parameter(description = "Variant public ID") @PathVariable String variantPublicId,
            @Valid @RequestBody ProductRequest.ProductVariantRequest request) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.updateVariant(storeId, productPublicId, variantPublicId, request));
    }

    @DeleteMapping("/{productPublicId}/variants/{variantPublicId}")
    @Operation(summary = "Remove variant from product", description = "Fails if this is the product's only variant.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<ProductResponse> deleteVariant(
            @AuthenticationPrincipal String userPublicId,
            @Parameter(description = "Store public ID") @PathVariable String storePublicId,
            @Parameter(description = "Product public ID") @PathVariable String productPublicId,
            @Parameter(description = "Variant public ID") @PathVariable String variantPublicId) {
        storeAccessService.ensureUserCanAccessStore(userPublicId, storePublicId);
        merchantPermissionService.ensureStorePermissionByPublicId(userPublicId, storePublicId, "products:write");
        Long storeId = catalogService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(catalogService.deleteVariant(storeId, productPublicId, variantPublicId));
    }

    private void ensureStoreAccess(Long storeId) {
        Long currentStore = CurrentTenant.getStoreId();
        if (currentStore != null && !currentStore.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("Store context mismatch");
        }
    }
}
