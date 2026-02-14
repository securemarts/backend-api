package com.shopper.domain.rider.service;

import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.catalog.service.FileStorageService;
import com.shopper.domain.logistics.entity.DeliveryOrder;
import com.shopper.domain.logistics.entity.DeliveryTrackingEvent;
import com.shopper.domain.logistics.entity.ProofOfDelivery;
import com.shopper.domain.logistics.entity.Rider;
import com.shopper.domain.logistics.event.DeliveryStatusChangedEvent;
import com.shopper.domain.logistics.repository.*;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.order.repository.OrderRepository;
import com.shopper.domain.rider.dto.CompleteDeliveryRequest;
import com.shopper.domain.rider.dto.RiderDeliveryResponse;
import com.shopper.domain.rider.dto.UpdateLocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiderDeliveryService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final RiderRepository riderRepository;
    private final DeliveryTrackingEventRepository trackingEventRepository;
    private final ProofOfDeliveryRepository proofOfDeliveryRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    /** Update rider's current location (for nearest-rider dispatch when they go available) */
    @Transactional
    public void updateRiderLocation(String riderPublicId, UpdateLocationRequest request) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        if (request.getLatitude() != null) rider.setCurrentLat(request.getLatitude());
        if (request.getLongitude() != null) rider.setCurrentLng(request.getLongitude());
        riderRepository.save(rider);
    }

    private void ensureRiderOwnsDelivery(String riderPublicId, DeliveryOrder d) {
        if (d.getRider() == null || !d.getRider().getPublicId().equals(riderPublicId)) {
            throw new ResourceNotFoundException("DeliveryOrder", d.getPublicId());
        }
    }

    @Transactional(readOnly = true)
    public List<RiderDeliveryResponse> getAssignedDeliveries(String riderPublicId) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        List<DeliveryOrder.DeliveryStatus> statuses = List.of(
                DeliveryOrder.DeliveryStatus.ASSIGNED,
                DeliveryOrder.DeliveryStatus.PICKED_UP,
                DeliveryOrder.DeliveryStatus.IN_TRANSIT
        );
        List<DeliveryOrder> list = deliveryOrderRepository.findByRiderIdAndStatusIn(rider.getId(), statuses);
        return list.stream().map(d -> toResponse(d)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RiderDeliveryResponse getDelivery(String riderPublicId, String deliveryOrderPublicId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        return toResponse(d);
    }

    @Transactional
    public RiderDeliveryResponse acceptDelivery(String riderPublicId, String deliveryOrderPublicId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException("Delivery is not in ASSIGNED status");
        }
        // Already assigned to this rider; accept just confirms (no state change or we could set a "acceptedAt")
        return toResponse(d);
    }

    @Transactional
    public void rejectDelivery(String riderPublicId, String deliveryOrderPublicId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException("Can only reject ASSIGNED delivery");
        }
        if (d.getRider() != null) {
            d.getRider().setAvailable(true);
        }
        String previousStatus = d.getStatus().name();
        d.setRider(null);
        d.setStatus(DeliveryOrder.DeliveryStatus.PENDING);
        deliveryOrderRepository.save(d);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, d.getStatus().name(), null));
    }

    @Transactional
    public RiderDeliveryResponse startDelivery(String riderPublicId, String deliveryOrderPublicId, boolean pickedUp) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException("Delivery must be ASSIGNED to start");
        }
        String previousStatus = d.getStatus().name();
        d.setStatus(pickedUp ? DeliveryOrder.DeliveryStatus.PICKED_UP : DeliveryOrder.DeliveryStatus.IN_TRANSIT);
        d = deliveryOrderRepository.save(d);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, d.getStatus().name(), d.getRider().getPublicId()));
        DeliveryTrackingEvent ev = new DeliveryTrackingEvent();
        ev.setDeliveryOrder(d);
        ev.setStatus(d.getStatus().name());
        trackingEventRepository.save(ev);
        return toResponse(d);
    }

    @Transactional
    public RiderDeliveryResponse updateLocation(String riderPublicId, String deliveryOrderPublicId, UpdateLocationRequest request) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.PICKED_UP && d.getStatus() != DeliveryOrder.DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException("Can only update location for PICKED_UP or IN_TRANSIT delivery");
        }
        DeliveryTrackingEvent ev = new DeliveryTrackingEvent();
        ev.setDeliveryOrder(d);
        ev.setStatus(d.getStatus().name());
        ev.setLatitude(request.getLatitude());
        ev.setLongitude(request.getLongitude());
        ev.setNote(request.getNote());
        trackingEventRepository.save(ev);
        // Update rider's current position for future nearest-rider dispatch
        if (request.getLatitude() != null && request.getLongitude() != null && d.getRider() != null) {
            d.getRider().setCurrentLat(request.getLatitude());
            d.getRider().setCurrentLng(request.getLongitude());
            riderRepository.save(d.getRider());
        }
        return toResponse(d);
    }

    @Transactional
    public RiderDeliveryResponse completeDelivery(String riderPublicId, String deliveryOrderPublicId, CompleteDeliveryRequest request) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.IN_TRANSIT && d.getStatus() != DeliveryOrder.DeliveryStatus.PICKED_UP) {
            throw new IllegalStateException("Delivery must be IN_TRANSIT or PICKED_UP to complete");
        }
        String previousStatus = d.getStatus().name();
        d.setStatus(DeliveryOrder.DeliveryStatus.DELIVERED);
        d.setDeliveredAt(Instant.now());
        if (d.getRider() != null) {
            d.getRider().setAvailable(true);
        }
        d = deliveryOrderRepository.save(d);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, DeliveryOrder.DeliveryStatus.DELIVERED.name(), d.getRider().getPublicId()));
        DeliveryTrackingEvent ev = new DeliveryTrackingEvent();
        ev.setDeliveryOrder(d);
        ev.setStatus(DeliveryOrder.DeliveryStatus.DELIVERED.name());
        trackingEventRepository.save(ev);
        if (request != null && request.getPodType() != null && !request.getPodType().isBlank()) {
            ProofOfDelivery pod = new ProofOfDelivery();
            pod.setDeliveryOrder(d);
            pod.setType(ProofOfDelivery.ProofType.valueOf(request.getPodType()));
            pod.setPayload(request.getPodPayload());
            proofOfDeliveryRepository.save(pod);
        }
        return toResponse(d);
    }

    @Transactional
    public RiderDeliveryResponse uploadProofOfDelivery(String riderPublicId, String deliveryOrderPublicId, String type, MultipartFile file) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        ensureRiderOwnsDelivery(riderPublicId, d);
        ProofOfDelivery.ProofType podType = ProofOfDelivery.ProofType.valueOf(type);
        String fileUrl = null;
        try {
            if (file != null && !file.isEmpty()) {
                fileUrl = fileStorageService.storePod(deliveryOrderPublicId, file);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store POD file", e);
        }
        ProofOfDelivery pod = new ProofOfDelivery();
        pod.setDeliveryOrder(d);
        pod.setType(podType);
        pod.setFileUrl(fileUrl);
        proofOfDeliveryRepository.save(pod);
        return toResponse(d);
    }

    private RiderDeliveryResponse toResponse(DeliveryOrder d) {
        String orderPublicId = orderRepository.findById(d.getOrderId()).map(o -> o.getPublicId()).orElse(null);
        String storePublicId = storeRepository.findById(d.getStoreId()).map(s -> s.getPublicId()).orElse(null);
        return RiderDeliveryResponse.builder()
                .publicId(d.getPublicId())
                .orderId(orderPublicId)
                .storeId(storePublicId)
                .pickupAddress(d.getPickupAddress())
                .deliveryAddress(d.getDeliveryAddress())
                .status(d.getStatus().name())
                .pricingAmount(d.getPricingAmount())
                .pricingCurrency(d.getPricingCurrency())
                .scheduledAt(d.getScheduledAt())
                .version(d.getVersion())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
