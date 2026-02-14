package com.shopper.domain.rider.controller;

import com.shopper.domain.catalog.service.FileStorageService;
import com.shopper.domain.logistics.repository.DeliveryOrderRepository;
import com.shopper.domain.rider.dto.CompleteDeliveryRequest;
import com.shopper.domain.rider.dto.RiderDeliveryResponse;
import com.shopper.common.dto.ApiResponse;
import com.shopper.domain.rider.dto.UpdateLocationRequest;
import com.shopper.domain.rider.service.RiderDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/rider/deliveries")
@RequiredArgsConstructor
@Tag(name = "Rider Deliveries", description = "Assigned deliveries, accept/reject, start, location, complete, POD")
@SecurityRequirement(name = "bearerAuth")
public class RiderDeliveryController {

    private final RiderDeliveryService riderDeliveryService;
    private final FileStorageService fileStorageService;
    private final DeliveryOrderRepository deliveryOrderRepository;

    @GetMapping
    @Operation(summary = "Get assigned deliveries", description = "List deliveries assigned to the rider (ASSIGNED, PICKED_UP, IN_TRANSIT)")
    public ResponseEntity<List<RiderDeliveryResponse>> getAssignedDeliveries(@AuthenticationPrincipal String riderPublicId) {
        return ResponseEntity.ok(riderDeliveryService.getAssignedDeliveries(riderPublicId));
    }

    @PatchMapping("/me/location")
    @Operation(summary = "Update my location", description = "Set rider's current position for dispatch (call when going available or periodically)")
    public ResponseEntity<?> updateMyLocation(
            @AuthenticationPrincipal String riderPublicId,
            @Valid @RequestBody UpdateLocationRequest request) {
        riderDeliveryService.updateRiderLocation(riderPublicId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{deliveryOrderPublicId}")
    @Operation(summary = "Get delivery details")
    public ResponseEntity<RiderDeliveryResponse> getDelivery(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId) {
        return ResponseEntity.ok(riderDeliveryService.getDelivery(riderPublicId, deliveryOrderPublicId));
    }

    @PostMapping("/{deliveryOrderPublicId}/accept")
    @Operation(summary = "Accept assigned delivery")
    public ResponseEntity<RiderDeliveryResponse> acceptDelivery(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId) {
        return ResponseEntity.ok(riderDeliveryService.acceptDelivery(riderPublicId, deliveryOrderPublicId));
    }

    @PostMapping("/{deliveryOrderPublicId}/reject")
    @Operation(summary = "Reject assigned delivery (unassign)")
    public ResponseEntity<?> rejectDelivery(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId) {
        riderDeliveryService.rejectDelivery(riderPublicId, deliveryOrderPublicId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{deliveryOrderPublicId}/start")
    @Operation(summary = "Start delivery", description = "Set status to PICKED_UP or IN_TRANSIT")
    public ResponseEntity<RiderDeliveryResponse> startDelivery(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId,
            @RequestParam(defaultValue = "true") boolean pickedUp) {
        return ResponseEntity.ok(riderDeliveryService.startDelivery(riderPublicId, deliveryOrderPublicId, pickedUp));
    }

    @PostMapping("/{deliveryOrderPublicId}/location")
    @Operation(summary = "Update location (tracking)")
    public ResponseEntity<RiderDeliveryResponse> updateLocation(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(riderDeliveryService.updateLocation(riderPublicId, deliveryOrderPublicId, request));
    }

    @PostMapping("/{deliveryOrderPublicId}/complete")
    @Operation(summary = "Complete delivery", description = "Mark DELIVERED, optional inline POD")
    public ResponseEntity<RiderDeliveryResponse> completeDelivery(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId,
            @RequestBody(required = false) CompleteDeliveryRequest request) {
        return ResponseEntity.ok(riderDeliveryService.completeDelivery(riderPublicId, deliveryOrderPublicId, request));
    }

    @PostMapping(value = "/{deliveryOrderPublicId}/pod", consumes = "multipart/form-data")
    @Operation(summary = "Upload proof of delivery (signature/photo)")
    public ResponseEntity<RiderDeliveryResponse> uploadPod(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId,
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile file) {
        return ResponseEntity.ok(riderDeliveryService.uploadProofOfDelivery(riderPublicId, deliveryOrderPublicId, type, file));
    }

    @GetMapping("/{deliveryOrderPublicId}/pod/{filename}")
    @Operation(summary = "Serve POD file (for uploaded photo/signature)")
    public ResponseEntity<InputStreamResource> servePodFile(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId,
            @PathVariable String filename) throws Exception {
        var d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId).orElse(null);
        if (d == null || d.getRider() == null || !d.getRider().getPublicId().equals(riderPublicId)) {
            return ResponseEntity.notFound().build();
        }
        Path file = fileStorageService.resolvePodFile(deliveryOrderPublicId, filename);
        if (file == null || !Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(new InputStreamResource(Files.newInputStream(file)));
    }
}
