package com.securemarts.domain.rider.service;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.common.util.GeoUtils;
import com.securemarts.domain.catalog.service.FileStorageService;
import com.securemarts.domain.logistics.entity.DeliveryOrder;
import com.securemarts.domain.logistics.entity.DeliveryTrackingEvent;
import com.securemarts.domain.logistics.entity.ProofOfDelivery;
import com.securemarts.domain.logistics.entity.Rider;
import com.securemarts.domain.logistics.event.DeliveryStatusChangedEvent;
import com.securemarts.domain.logistics.repository.*;
import com.securemarts.domain.onboarding.entity.StoreProfile;
import com.securemarts.domain.onboarding.repository.StoreProfileRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.order.repository.OrderRepository;
import com.securemarts.domain.rider.dto.AvailableDeliveryResponse;
import com.securemarts.domain.rider.dto.CompleteDeliveryRequest;
import com.securemarts.domain.rider.dto.RiderDeliveryResponse;
import com.securemarts.domain.rider.dto.UpdateLocationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final StoreProfileRepository storeProfileRepository;
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
    public List<AvailableDeliveryResponse> getAvailableDeliveries(String riderPublicId, java.math.BigDecimal latitude, java.math.BigDecimal longitude, Double radiusKm) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        if (rider.getZone() == null) {
            return List.of();
        }
        if (rider.getVerificationStatus() != Rider.VerificationStatus.APPROVED) {
            return List.of();
        }
        List<Long> storeIds = storeProfileRepository.findStoreIdsByZoneId(rider.getZone().getId());
        if (storeIds.isEmpty()) {
            return List.of();
        }
        List<DeliveryOrder> pending = deliveryOrderRepository.findByStoreIdInAndStatus(storeIds, DeliveryOrder.DeliveryStatus.PENDING);
        if (pending.isEmpty()) {
            return List.of();
        }
        Map<Long, StoreProfile> storeIdToProfile = storeProfileRepository.findByStore_IdIn(storeIds).stream()
                .collect(Collectors.toMap(p -> p.getStore().getId(), p -> p));
        double radius = radiusKm != null && radiusKm > 0 ? radiusKm : 50.0;
        double lat = latitude != null ? latitude.doubleValue() : (rider.getCurrentLat() != null ? rider.getCurrentLat().doubleValue() : 0);
        double lng = longitude != null ? longitude.doubleValue() : (rider.getCurrentLng() != null ? rider.getCurrentLng().doubleValue() : 0);

        List<AvailableDeliveryResponse> result = new ArrayList<>();
        for (DeliveryOrder d : pending) {
            StoreProfile profile = storeIdToProfile.get(d.getStoreId());
            if (profile == null || profile.getLatitude() == null || profile.getLongitude() == null) continue;
            double distance = GeoUtils.distanceKm(lat, lng, profile.getLatitude().doubleValue(), profile.getLongitude().doubleValue());
            if (distance > radius) continue;
            String orderPublicId = orderRepository.findById(d.getOrderId()).map(o -> o.getPublicId()).orElse(null);
            String storePublicId = storeRepository.findById(d.getStoreId()).map(s -> s.getPublicId()).orElse(null);
            result.add(AvailableDeliveryResponse.builder()
                    .publicId(d.getPublicId())
                    .orderId(orderPublicId)
                    .storeId(storePublicId)
                    .pickupAddress(d.getPickupAddress())
                    .deliveryAddress(d.getDeliveryAddress())
                    .status(d.getStatus().name())
                    .pricingAmount(d.getPricingAmount())
                    .pricingCurrency(d.getPricingCurrency())
                    .scheduledAt(d.getScheduledAt())
                    .createdAt(d.getCreatedAt())
                    .distanceKm(Math.round(distance * 100.0) / 100.0)
                    .build());
        }
        result.sort((a, b) -> Double.compare(a.getDistanceKm() != null ? a.getDistanceKm() : 0, b.getDistanceKm() != null ? b.getDistanceKm() : 0));
        return result;
    }

    @Transactional
    public RiderDeliveryResponse claimDelivery(String riderPublicId, String deliveryOrderPublicId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Only PENDING deliveries can be claimed");
        }
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        if (rider.getZone() == null) {
            throw new IllegalStateException("Rider has no zone assigned");
        }
        if (rider.getVerificationStatus() != Rider.VerificationStatus.APPROVED) {
            throw new IllegalStateException("Rider is not approved");
        }
        if (!rider.isAvailable()) {
            throw new IllegalStateException("Rider is not available");
        }
        StoreProfile storeProfile = storeProfileRepository.findByStoreIdWithZone(d.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("StoreProfile", "storeId=" + d.getStoreId()));
        if (storeProfile.getZone() == null || !storeProfile.getZone().getId().equals(rider.getZone().getId())) {
            throw new IllegalStateException("Delivery is not in rider's zone");
        }
        String previousStatus = d.getStatus().name();
        d.setRider(rider);
        d.setStatus(DeliveryOrder.DeliveryStatus.ASSIGNED);
        rider.setAvailable(false);
        deliveryOrderRepository.save(d);
        riderRepository.save(rider);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, d.getStatus().name(), rider.getPublicId()));
        return toResponse(d);
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
