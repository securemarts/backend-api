package com.securemarts.domain.catalog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemarts.domain.catalog.dto.*;
import com.securemarts.domain.catalog.service.CollectionService;
import com.securemarts.domain.catalog.service.FileStorageService;
import com.securemarts.security.CurrentTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/stores/{storePublicId}/collections")
@RequiredArgsConstructor
@Tag(name = "Collections", description = "Create and list collections for a store")
@SecurityRequirement(name = "bearerAuth")
public class CollectionController {

    private final CollectionService collectionService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List collections", description = "List all collections for the store")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<List<CollectionResponse>> list(@PathVariable String storePublicId) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(collectionService.listByStore(storeId));
    }

    @GetMapping("/{collectionPublicId}")
    @Operation(summary = "Get collection")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> get(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(collectionService.get(storeId, collectionPublicId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create collection",
            description = "Create manual or smart collection. "
                    + "Form fields: title (required), handle, description, collectionType (manual|smart), "
                    + "conditionsOperator (all|any), rules (JSON array), productIds (comma-separated). "
                    + "Optionally upload a collection image as 'image' part.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> create(
            @PathVariable String storePublicId,
            @RequestParam String title,
            @RequestParam(required = false) String handle,
            @RequestParam(required = false) String description,
            @Parameter(description = "manual or smart", schema = @Schema(allowableValues = {"manual", "smart"}))
            @RequestParam(required = false, defaultValue = "manual") String collectionType,
            @Parameter(description = "all or any", schema = @Schema(allowableValues = {"all", "any"}))
            @RequestParam(required = false) String conditionsOperator,
            @Parameter(description = "Rules for smart collections. JSON array of objects, each with: "
                    + "field (title, product_type, vendor, tag, price, compare_at_price, weight, inventory_stock, variant_title), "
                    + "operator (equals, not_equals, contains, starts_with, ends_with, greater_than, less_than), "
                    + "value (string or number to compare). "
                    + "Example: [{\"field\":\"tag\",\"operator\":\"equals\",\"value\":\"vintage\"}]",
                    schema = @Schema(example = "[{\"field\":\"tag\",\"operator\":\"equals\",\"value\":\"vintage\"}]"))
            @RequestParam(required = false) String rules,
            @Parameter(description = "Product public IDs for manual collections (comma-separated). "
                    + "Example: 3533a73d-661f-4a97-b2a8-86bd54ad3547,a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestParam(required = false) String productIds,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);

        CreateCollectionRequest request = new CreateCollectionRequest();
        request.setTitle(title);
        request.setHandle(handle);
        request.setDescription(description);
        request.setCollectionType(collectionType);
        request.setConditionsOperator(conditionsOperator);
        if (rules != null && !rules.isBlank()) {
            request.setRules(objectMapper.readValue(rules, new TypeReference<List<CollectionRuleRequest>>() {}));
        }
        if (productIds != null && !productIds.isBlank()) {
            request.setProductIds(Stream.of(productIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }

        String imageUrl = fileStorageService.storeCollectionImage(storePublicId, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.create(storeId, request, imageUrl));
    }

    @PutMapping(value = "/{collectionPublicId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update collection",
            description = "Update collection fields. "
                    + "Form fields: title, handle, description, collectionType, conditionsOperator, "
                    + "rules (JSON array), productIds (comma-separated). "
                    + "Optionally upload a new collection image as 'image' part.")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> update(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String handle,
            @RequestParam(required = false) String description,
            @Parameter(description = "manual or smart", schema = @Schema(allowableValues = {"manual", "smart"}))
            @RequestParam(required = false) String collectionType,
            @Parameter(description = "all or any", schema = @Schema(allowableValues = {"all", "any"}))
            @RequestParam(required = false) String conditionsOperator,
            @Parameter(description = "Rules for smart collections (replaces existing). JSON array of objects, each with: "
                    + "field (title, product_type, vendor, tag, price, compare_at_price, weight, inventory_stock, variant_title), "
                    + "operator (equals, not_equals, contains, starts_with, ends_with, greater_than, less_than), "
                    + "value (string or number to compare). "
                    + "Example: [{\"field\":\"tag\",\"operator\":\"equals\",\"value\":\"vintage\"}]",
                    schema = @Schema(example = "[{\"field\":\"tag\",\"operator\":\"equals\",\"value\":\"vintage\"}]"))
            @RequestParam(required = false) String rules,
            @Parameter(description = "Product public IDs for manual collections (comma-separated). "
                    + "Example: 3533a73d-661f-4a97-b2a8-86bd54ad3547,a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @RequestParam(required = false) String productIds,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);

        UpdateCollectionRequest request = new UpdateCollectionRequest();
        request.setTitle(title);
        request.setHandle(handle);
        request.setDescription(description);
        request.setCollectionType(collectionType);
        request.setConditionsOperator(conditionsOperator);
        if (rules != null && !rules.isBlank()) {
            request.setRules(objectMapper.readValue(rules, new TypeReference<List<CollectionRuleRequest>>() {}));
        }
        if (productIds != null && !productIds.isBlank()) {
            request.setProductIds(Stream.of(productIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        }

        String imageUrl = fileStorageService.storeCollectionImage(storePublicId, image);
        return ResponseEntity.ok(collectionService.update(storeId, collectionPublicId, request, imageUrl));
    }

    @PostMapping(value = "/{collectionPublicId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload collection image", description = "Upload or replace the collection image")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> uploadImage(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId,
            @RequestPart("image") MultipartFile image) throws IOException {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        String imageUrl = fileStorageService.storeCollectionImage(storePublicId, image);
        return ResponseEntity.ok(collectionService.updateImage(storeId, collectionPublicId, imageUrl));
    }

    @DeleteMapping("/{collectionPublicId}/image")
    @Operation(summary = "Remove collection image", description = "Remove the image from a collection")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<CollectionResponse> removeImage(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(collectionService.updateImage(storeId, collectionPublicId, null));
    }

    @GetMapping("/{collectionPublicId}/products")
    @Operation(summary = "List collection products", description = "Paginated, ordered by position")
    @PreAuthorize("hasAuthority('SCOPE_products:read') or hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Page<CollectionProductItemResponse>> listProducts(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId,
            Pageable pageable) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        return ResponseEntity.ok(collectionService.listCollectionProducts(storeId, collectionPublicId, pageable));
    }

    @PostMapping("/{collectionPublicId}/products")
    @Operation(summary = "Add products to manual collection")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Void> addProducts(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId,
            @Valid @RequestBody AddCollectionProductsRequest request) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        collectionService.addProducts(storeId, collectionPublicId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{collectionPublicId}/products/{productPublicId}")
    @Operation(summary = "Remove product from manual collection")
    @PreAuthorize("hasAuthority('SCOPE_products:write') or hasRole('MERCHANT_OWNER') or hasRole('MERCHANT_STAFF')")
    public ResponseEntity<Void> removeProduct(
            @PathVariable String storePublicId,
            @PathVariable String collectionPublicId,
            @PathVariable String productPublicId) {
        Long storeId = collectionService.resolveStoreId(storePublicId);
        ensureStoreAccess(storeId);
        collectionService.removeProduct(storeId, collectionPublicId, productPublicId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void ensureStoreAccess(Long storeId) {
        Long currentStore = CurrentTenant.getStoreId();
        if (currentStore != null && !currentStore.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("Store context mismatch");
        }
    }
}
