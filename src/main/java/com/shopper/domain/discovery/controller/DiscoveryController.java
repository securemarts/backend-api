package com.shopper.domain.discovery.controller;

import com.shopper.common.dto.PageResponse;
import com.shopper.domain.discovery.dto.DeliveryEtaResponse;
import com.shopper.domain.discovery.dto.LocationAvailabilityResponse;
import com.shopper.domain.discovery.dto.LocationSummaryResponse;
import com.shopper.domain.discovery.dto.StoreDiscoveryResponse;
import com.shopper.domain.discovery.service.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Search stores", description = "By name/brand (q), city/state, or geo (lat, lng, radiusKm). Sort by distance when lat/lng provided. Use includeLocations=true to embed location list (publicId, name, address) per store.")
    public ResponseEntity<PageResponse<StoreDiscoveryResponse>> searchStores(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(required = false) BigDecimal radiusKm,
            @RequestParam(defaultValue = "false") boolean includeLocations,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(discoveryService.searchStores(q, state, city, lat, lng, radiusKm, includeLocations, page, size));
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
            @RequestParam(required = false) List<String> variantIds) {
        return ResponseEntity.ok(discoveryService.getLocationAvailability(storePublicId, locationPublicId, variantIds));
    }

    @GetMapping("/stores/{storePublicId}/delivery-eta")
    @Operation(summary = "Delivery ETA", description = "Estimated delivery time for store to customer location (lat/lng or city/state)")
    public ResponseEntity<DeliveryEtaResponse> getDeliveryEta(
            @PathVariable String storePublicId,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state) {
        return ResponseEntity.ok(discoveryService.getDeliveryEta(storePublicId, lat, lng, city, state));
    }
}
