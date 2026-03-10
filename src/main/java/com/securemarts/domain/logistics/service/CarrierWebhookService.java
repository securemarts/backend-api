package com.securemarts.domain.logistics.service;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.logistics.dto.CarrierWebhookPayload;
import com.securemarts.domain.logistics.entity.DeliveryOrder;
import com.securemarts.domain.logistics.event.DeliveryStatusChangedEvent;
import com.securemarts.domain.logistics.repository.DeliveryOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Handles carrier webhooks (Aramex, Sendstack, etc.): map partner status to DeliveryStatus,
 * update DeliveryOrder, publish DeliveryStatusChangedEvent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CarrierWebhookService {

    private static final Map<String, DeliveryOrder.DeliveryStatus> STATUS_MAP = Map.ofEntries(
            Map.entry("PENDING", DeliveryOrder.DeliveryStatus.PENDING),
            Map.entry("ASSIGNED", DeliveryOrder.DeliveryStatus.ASSIGNED),
            Map.entry("PICKED_UP", DeliveryOrder.DeliveryStatus.PICKED_UP),
            Map.entry("PICKUP", DeliveryOrder.DeliveryStatus.PICKED_UP),
            Map.entry("IN_TRANSIT", DeliveryOrder.DeliveryStatus.IN_TRANSIT),
            Map.entry("TRANSIT", DeliveryOrder.DeliveryStatus.IN_TRANSIT),
            Map.entry("DELIVERED", DeliveryOrder.DeliveryStatus.DELIVERED),
            Map.entry("DELIVERY", DeliveryOrder.DeliveryStatus.DELIVERED),
            Map.entry("FAILED", DeliveryOrder.DeliveryStatus.FAILED),
            Map.entry("CANCELLED", DeliveryOrder.DeliveryStatus.FAILED),
            Map.entry("RETURNED", DeliveryOrder.DeliveryStatus.RETURNED),
            Map.entry("RETURN", DeliveryOrder.DeliveryStatus.RETURNED)
    );

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handleStatusUpdate(String carrierCode, CarrierWebhookPayload payload) {
        if (payload.getExternalShipmentId() == null || payload.getExternalShipmentId().isBlank()) {
            throw new IllegalArgumentException("externalShipmentId is required");
        }
        DeliveryOrder d = deliveryOrderRepository.findByCarrierCodeAndExternalShipmentId(carrierCode, payload.getExternalShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", "carrier=" + carrierCode + ", externalId=" + payload.getExternalShipmentId()));
        String previousStatus = d.getStatus().name();
        DeliveryOrder.DeliveryStatus newStatus = mapStatus(payload.getStatus());
        if (newStatus == null) {
            log.debug("Carrier webhook: unknown status '{}' for {}", payload.getStatus(), payload.getExternalShipmentId());
            return;
        }
        d.setStatus(newStatus);
        if (payload.getTrackingUrl() != null && !payload.getTrackingUrl().isBlank()) {
            d.setTrackingUrl(payload.getTrackingUrl());
        }
        if (newStatus == DeliveryOrder.DeliveryStatus.DELIVERED) {
            d.setDeliveredAt(Instant.now());
        }
        if (newStatus == DeliveryOrder.DeliveryStatus.FAILED && payload.getStatus() != null) {
            d.setFailedReason("Carrier reported: " + payload.getStatus());
        }
        deliveryOrderRepository.save(d);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, newStatus.name(), d.getRider() != null ? d.getRider().getPublicId() : null));
        log.info("Carrier webhook: {} delivery {} {} -> {}", carrierCode, d.getPublicId(), previousStatus, newStatus);
    }

    private static DeliveryOrder.DeliveryStatus mapStatus(String partnerStatus) {
        if (partnerStatus == null || partnerStatus.isBlank()) return null;
        return STATUS_MAP.get(partnerStatus.toUpperCase().replace(" ", "_"));
    }
}
