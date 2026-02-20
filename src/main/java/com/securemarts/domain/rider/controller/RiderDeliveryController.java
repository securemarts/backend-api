package com.securemarts.domain.rider.controller;

import com.securemarts.domain.catalog.service.FileStorageService;
import com.securemarts.domain.logistics.entity.ProofOfDelivery;
import com.securemarts.domain.logistics.repository.DeliveryOrderRepository;
import com.securemarts.domain.logistics.repository.ProofOfDeliveryRepository;
import com.securemarts.domain.rider.dto.AvailableDeliveryResponse;
import com.securemarts.domain.rider.dto.CompleteDeliveryRequest;
import com.securemarts.domain.rider.dto.RiderDeliveryResponse;
import com.securemarts.common.dto.ApiResponse;
import com.securemarts.domain.rider.dto.UpdateLocationRequest;
import com.securemarts.domain.rider.service.RiderDeliveryService;
import com.securemarts.domain.rider.sse.RiderSseRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rider/deliveries")
@RequiredArgsConstructor
@Tag(name = "Rider Deliveries", description = "Assigned deliveries, accept/reject, start, location, complete, POD")
@SecurityRequirement(name = "bearerAuth")
public class RiderDeliveryController {

    private final RiderDeliveryService riderDeliveryService;
    private final FileStorageService fileStorageService;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final ProofOfDeliveryRepository proofOfDeliveryRepository;
    private final RiderSseRegistry riderSseRegistry;

    @GetMapping
    @Operation(summary = "Get assigned deliveries", description = "List deliveries assigned to the rider (ASSIGNED, PICKED_UP, IN_TRANSIT)")
    public ResponseEntity<List<RiderDeliveryResponse>> getAssignedDeliveries(@AuthenticationPrincipal String riderPublicId) {
        return ResponseEntity.ok(riderDeliveryService.getAssignedDeliveries(riderPublicId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available deliveries to claim", description = "PENDING deliveries within radius of rider (same zone). Use when autoAssign=false.")
    public ResponseEntity<List<AvailableDeliveryResponse>> getAvailableDeliveries(
            @AuthenticationPrincipal String riderPublicId,
            @RequestParam(required = false) java.math.BigDecimal latitude,
            @RequestParam(required = false) java.math.BigDecimal longitude,
            @RequestParam(required = false) Double radiusKm) {
        return ResponseEntity.ok(riderDeliveryService.getAvailableDeliveries(riderPublicId, latitude, longitude, radiusKm));
    }

    @PostMapping("/{deliveryOrderPublicId}/claim")
    @Operation(summary = "Claim a PENDING delivery", description = "Assign this delivery to the rider (rider must be available and in same zone)")
    public ResponseEntity<RiderDeliveryResponse> claimDelivery(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId) {
        return ResponseEntity.ok(riderDeliveryService.claimDelivery(riderPublicId, deliveryOrderPublicId));
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    @Operation(summary = "SSE stream for delivery events", description = "Server-sent events: delivery_assigned, delivery_available. Keep connection open to receive real-time updates.")
    public SseEmitter stream(@AuthenticationPrincipal String riderPublicId) {
        SseEmitter emitter = riderSseRegistry.createAndRegister(riderPublicId);
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("type", "connected", "riderPublicId", riderPublicId)));
        } catch (IOException e) {
            riderSseRegistry.remove(riderPublicId, emitter);
        }
        return emitter;
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
            @Parameter(description = "POD type", required = true, schema = @Schema(allowableValues = {"SIGNATURE", "PHOTO"})) @RequestParam String type,
            @RequestParam(required = false) MultipartFile file) {
        return ResponseEntity.ok(riderDeliveryService.uploadProofOfDelivery(riderPublicId, deliveryOrderPublicId, type, file));
    }

    @GetMapping("/{deliveryOrderPublicId}/pod/{filename}")
    @Operation(summary = "Redirect to POD file (stored in Spaces). Returns 302 to the file URL or 404 if not found.")
    public ResponseEntity<Void> servePodFile(
            @AuthenticationPrincipal String riderPublicId,
            @PathVariable String deliveryOrderPublicId,
            @PathVariable String filename) {
        var d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId).orElse(null);
        if (d == null || d.getRider() == null || !d.getRider().getPublicId().equals(riderPublicId)) {
            return ResponseEntity.notFound().build();
        }
        List<ProofOfDelivery> pods = proofOfDeliveryRepository.findByDeliveryOrderId(d.getId());
        String fileUrl = pods.stream()
                .map(ProofOfDelivery::getFileUrl)
                .filter(url -> url != null && !url.isBlank() && (url.endsWith(filename) || url.contains("/" + filename)))
                .findFirst()
                .orElse(null);
        if (fileUrl == null || !fileUrl.startsWith("http")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(fileUrl)).build();
    }
}
