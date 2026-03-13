package com.securemarts.domain.discovery.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.discovery.dto.DeliveryEtaResponse;
import com.securemarts.domain.discovery.dto.LocationAvailabilityResponse;
import com.securemarts.domain.discovery.dto.LocationSummaryResponse;
import com.securemarts.domain.discovery.dto.StoreDiscoveryResponse;
import com.securemarts.domain.discovery.service.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
@Tag(name = "Discovery", description = "Store/location search, inventory availability, delivery ETA (public)")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    @GetMapping("/stores")
    @Operation(summary = "Search stores", description = "By name/brand (q), city/state, or geo (lat, lng, radiusKm). Sort by distance when lat/lng provided; use sort=rating for highest-rated first. Use includeLocations=true to embed location list (publicId, name, address) per store.")
    public ResponseEntity<PageResponse<StoreDiscoveryResponse>> searchStores(
            @Parameter(description = "Search by store name or brand", schema = @Schema(example = "Acme")) @RequestParam(required = false) String q,
            @Parameter(description = "Filter by city", schema = @Schema(example = "Lagos")) @RequestParam(required = false) String city,
            @Parameter(description = "Filter by state", schema = @Schema(example = "Lagos")) @RequestParam(required = false) String state,
            @Parameter(description = "Latitude for geo search", schema = @Schema(example = "6.5244")) @RequestParam(required = false) BigDecimal lat,
            @Parameter(description = "Longitude for geo search", schema = @Schema(example = "3.3792")) @RequestParam(required = false) BigDecimal lng,
            @Parameter(description = "Search radius in km (default: 10)", schema = @Schema(example = "5")) @RequestParam(required = false) BigDecimal radiusKm,
            @Parameter(description = "Include store locations in response") @RequestParam(defaultValue = "false") boolean includeLocations,
            @Parameter(description = "Sort order: distance (default when lat/lng provided) or rating", schema = @Schema(allowableValues = {"distance", "rating"})) @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(discoveryService.searchStores(q, state, city, lat, lng, radiusKm, includeLocations, sort, page, size));
    }

    @GetMapping("/stores/{storePublicId}/locations")
    @Operation(summary = "List locations for store", description = "Public list of locations (pick-up points, branches). Use locationPublicId in the availability endpoint.")
    public ResponseEntity<List<LocationSummaryResponse>> listLocationsForStore(@PathVariable String storePublicId) {
        return ResponseEntity.ok(discoveryService.listLocationsForStore(storePublicId));
    }

    @GetMapping("/stores/{storePublicId}/locations/{locationPublicId}/availability")
    @Operation(summary = "Inventory availability by location", description = "Returns variants with quantity and product details (title, price, image) for UI. Pass variantIds for specific variants, or omit for all at location.")
    public ResponseEntity<LocationAvailabilityResponse> getLocationAvailability(
            @PathVariable String storePublicId,
            @PathVariable String locationPublicId,
            @Parameter(description = "Filter by variant public IDs (comma-separated). Omit for all variants at this location.") @RequestParam(required = false) List<String> variantIds) {
        return ResponseEntity.ok(discoveryService.getLocationAvailability(storePublicId, locationPublicId, variantIds));
    }

    @GetMapping("/stores/{storePublicId}/delivery-eta")
    @Operation(summary = "Delivery ETA", description = "Estimated delivery time for store to customer location (lat/lng or city/state)")
    public ResponseEntity<DeliveryEtaResponse> getDeliveryEta(
            @PathVariable String storePublicId,
            @Parameter(description = "Customer latitude", schema = @Schema(example = "6.4541")) @RequestParam(required = false) BigDecimal lat,
            @Parameter(description = "Customer longitude", schema = @Schema(example = "3.4233")) @RequestParam(required = false) BigDecimal lng,
            @Parameter(description = "Customer city", schema = @Schema(example = "Lagos")) @RequestParam(required = false) String city,
            @Parameter(description = "Customer state", schema = @Schema(example = "Lagos")) @RequestParam(required = false) String state) {
        return ResponseEntity.ok(discoveryService.getDeliveryEta(storePublicId, lat, lng, city, state));
    }
}
