package com.shopper.domain.admin.controller;

import com.shopper.common.dto.ApiResponse;
import com.shopper.common.dto.PageResponse;
import com.shopper.domain.logistics.dto.*;
import com.shopper.domain.logistics.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/logistics")
@RequiredArgsConstructor
@Tag(name = "Admin - Logistics", description = "Service zones (Chowdeck model) and riders")
@SecurityRequirement(name = "bearerAuth")
public class AdminLogisticsController {

    private final LogisticsService logisticsService;

    // --- Service zones ---
    @GetMapping("/service-zones")
    @Operation(summary = "List service zones")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER') or hasRole('SUPPORT')")
    public ResponseEntity<PageResponse<ServiceZoneResponse>> listServiceZones(
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(logisticsService.listServiceZones(city, pageable));
    }

    @PostMapping("/service-zones")
    @Operation(summary = "Create service zone")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<ServiceZoneResponse> createServiceZone(@Valid @RequestBody CreateServiceZoneRequest request) {
        return ResponseEntity.ok(logisticsService.createServiceZone(request));
    }

    @GetMapping("/service-zones/{zonePublicId}")
    @Operation(summary = "Get service zone")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER') or hasRole('SUPPORT')")
    public ResponseEntity<ServiceZoneResponse> getServiceZone(@PathVariable String zonePublicId) {
        return ResponseEntity.ok(logisticsService.getServiceZone(zonePublicId));
    }

    @PatchMapping("/service-zones/{zonePublicId}")
    @Operation(summary = "Update service zone")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<ServiceZoneResponse> updateServiceZone(
            @PathVariable String zonePublicId,
            @Valid @RequestBody UpdateServiceZoneRequest request) {
        return ResponseEntity.ok(logisticsService.updateServiceZone(zonePublicId, request));
    }

    @PatchMapping("/stores/{storePublicId}/service-zone")
    @Operation(summary = "Assign store to service zone", description = "Set which zone a store delivers in. Required before creating delivery orders.")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<?> setStoreServiceZone(
            @PathVariable String storePublicId,
            @RequestBody SetStoreZoneRequest request) {
        logisticsService.setStoreServiceZone(storePublicId, request != null ? request.getZonePublicId() : null);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- Riders ---
    @GetMapping("/riders")
    @Operation(summary = "List riders")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER') or hasRole('SUPPORT')")
    public ResponseEntity<PageResponse<RiderResponse>> listRiders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String zonePublicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(logisticsService.listRiders(status, zonePublicId, pageable));
    }

    @PostMapping("/riders")
    @Operation(summary = "Create rider")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<RiderResponse> createRider(@Valid @RequestBody CreateRiderRequest request) {
        return ResponseEntity.ok(logisticsService.createRider(request));
    }

    @GetMapping("/riders/{riderPublicId}")
    @Operation(summary = "Get rider")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER') or hasRole('SUPPORT')")
    public ResponseEntity<RiderResponse> getRider(@PathVariable String riderPublicId) {
        return ResponseEntity.ok(logisticsService.getRider(riderPublicId));
    }

    @PatchMapping("/riders/{riderPublicId}")
    @Operation(summary = "Update rider")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<RiderResponse> updateRider(
            @PathVariable String riderPublicId,
            @Valid @RequestBody UpdateRiderRequest request) {
        return ResponseEntity.ok(logisticsService.updateRider(riderPublicId, request));
    }
}
