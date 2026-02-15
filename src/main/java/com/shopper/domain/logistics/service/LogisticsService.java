package com.shopper.domain.logistics.service;

import com.shopper.common.dto.PageResponse;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.common.util.GeoUtils;
import com.shopper.domain.logistics.dto.*;
import com.shopper.domain.logistics.entity.*;
import com.shopper.domain.logistics.repository.*;
import com.shopper.domain.onboarding.entity.StoreProfile;
import com.shopper.domain.onboarding.repository.StoreProfileRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.onboarding.service.SubscriptionLimitsService;
import com.shopper.domain.order.entity.Order;
import com.shopper.domain.logistics.event.DeliveryStatusChangedEvent;
import com.shopper.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final ServiceZoneRepository serviceZoneRepository;
    private final StoreProfileRepository storeProfileRepository;
    private final StoreRepository storeRepository;
    private final RiderRepository riderRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriptionLimitsService subscriptionLimitsService;

    /** Assign a store to a service zone (required for delivery creation) */
    @Transactional
    public void setStoreServiceZone(String storePublicId, String zonePublicId) {
        var store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        StoreProfile profile = storeProfileRepository.findByStoreIdWithZone(store.getId())
                .orElseThrow(() -> new ResourceNotFoundException("StoreProfile", "storeId=" + store.getId()));
        ServiceZone zone = zonePublicId == null || zonePublicId.isBlank()
                ? null
                : serviceZoneRepository.findByPublicId(zonePublicId)
                        .orElseThrow(() -> new ResourceNotFoundException("ServiceZone", zonePublicId));
        profile.setZone(zone);
        storeProfileRepository.save(profile);
    }

    // --- Service zones (Chowdeck: radius-based, base_fee + per_km_fee) ---
    @Transactional(readOnly = true)
    public PageResponse<ServiceZoneResponse> listServiceZones(String city, Pageable pageable) {
        Page<ServiceZone> page = (city != null && !city.isBlank())
                ? serviceZoneRepository.findByCityAndActiveTrue(city, pageable)
                : serviceZoneRepository.findByActiveTrue(pageable);
        return PageResponse.of(page.map(ServiceZoneResponse::from));
    }

    @Transactional
    public ServiceZoneResponse createServiceZone(CreateServiceZoneRequest request) {
        ServiceZone z = new ServiceZone();
        z.setName(request.getName());
        z.setCity(request.getCity());
        z.setCenterLat(request.getCenterLat());
        z.setCenterLng(request.getCenterLng());
        z.setRadiusKm(request.getRadiusKm());
        z.setBaseFee(request.getBaseFee());
        z.setPerKmFee(request.getPerKmFee());
        z.setMaxDistanceKm(request.getMaxDistanceKm());
        z.setMinOrderAmount(request.getMinOrderAmount());
        z.setSurgeEnabled(request.isSurgeEnabled());
        z.setActive(request.isActive());
        z = serviceZoneRepository.save(z);
        return ServiceZoneResponse.from(z);
    }

    @Transactional(readOnly = true)
    public ServiceZoneResponse getServiceZone(String publicId) {
        ServiceZone z = serviceZoneRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceZone", publicId));
        return ServiceZoneResponse.from(z);
    }

    @Transactional
    public ServiceZoneResponse updateServiceZone(String publicId, UpdateServiceZoneRequest request) {
        ServiceZone z = serviceZoneRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceZone", publicId));
        if (request.getName() != null) z.setName(request.getName());
        if (request.getCity() != null) z.setCity(request.getCity());
        if (request.getCenterLat() != null) z.setCenterLat(request.getCenterLat());
        if (request.getCenterLng() != null) z.setCenterLng(request.getCenterLng());
        if (request.getRadiusKm() != null) z.setRadiusKm(request.getRadiusKm());
        if (request.getBaseFee() != null) z.setBaseFee(request.getBaseFee());
        if (request.getPerKmFee() != null) z.setPerKmFee(request.getPerKmFee());
        if (request.getMaxDistanceKm() != null) z.setMaxDistanceKm(request.getMaxDistanceKm());
        if (request.getMinOrderAmount() != null) z.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getSurgeEnabled() != null) z.setSurgeEnabled(request.getSurgeEnabled());
        if (request.getActive() != null) z.setActive(request.getActive());
        z = serviceZoneRepository.save(z);
        return ServiceZoneResponse.from(z);
    }

    // --- Delivery orders (Chowdeck: zone check → fee → nearest rider) ---
    @Transactional
    public DeliveryOrderResponse createDeliveryOrder(Long storeId, CreateDeliveryOrderRequest request) {
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        var effectivePlan = subscriptionLimitsService.getEffectivePlan(store.getBusiness());
        if (!subscriptionLimitsService.isDeliveryEnabled(effectivePlan)) {
            throw new IllegalArgumentException("Your Pro trial has ended. Subscribe to Pro to continue using delivery.");
        }
        Order order = orderRepository.findByPublicId(request.getOrderPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderPublicId()));
        if (!order.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("Order does not belong to this store");
        }
        if (deliveryOrderRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalArgumentException("Delivery order already exists for this order");
        }

        StoreProfile profile = storeProfileRepository.findByStoreIdWithZone(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("StoreProfile", "storeId=" + storeId));
        ServiceZone zone = profile.getZone();
        if (zone == null) {
            throw new IllegalArgumentException("Store is not assigned to a service zone");
        }
        if (!zone.isActive()) {
            throw new IllegalArgumentException("Service zone is not active");
        }

        BigDecimal customerLat = request.getDeliveryLat();
        BigDecimal customerLng = request.getDeliveryLng();

        // Step 1: Check customer inside zone (distance from zone center to customer)
        double distanceToZoneCenter = GeoUtils.distanceKm(
                zone.getCenterLat(), zone.getCenterLng(),
                customerLat, customerLng
        );
        if (distanceToZoneCenter > zone.getRadiusKm().doubleValue()) {
            throw new IllegalArgumentException("Customer location is outside delivery zone (distance " + String.format("%.2f", distanceToZoneCenter) + " km > radius " + zone.getRadiusKm() + " km)");
        }

        BigDecimal storeLat = profile.getLatitude();
        BigDecimal storeLng = profile.getLongitude();
        if (storeLat == null || storeLng == null) {
            throw new IllegalArgumentException("Store has no latitude/longitude set");
        }

        // Step 2: Delivery distance (store → customer) and fee
        double deliveryDistanceKm = GeoUtils.distanceKm(storeLat, storeLng, customerLat, customerLng);
        if (zone.getMaxDistanceKm() != null && deliveryDistanceKm > zone.getMaxDistanceKm().doubleValue()) {
            throw new IllegalArgumentException("Delivery distance too far (max " + zone.getMaxDistanceKm() + " km)");
        }
        BigDecimal fee = zone.getBaseFee().add(
                zone.getPerKmFee().multiply(BigDecimal.valueOf(deliveryDistanceKm)).setScale(4, RoundingMode.HALF_UP)
        );

        DeliveryOrder d = new DeliveryOrder();
        d.setOrderId(order.getId());
        d.setStoreId(storeId);
        d.setDeliveryAddress(request.getDeliveryAddress());
        d.setPickupAddress(request.getPickupAddress());
        d.setDeliveryLat(customerLat);
        d.setDeliveryLng(customerLng);
        d.setPricingAmount(fee);
        d.setPricingCurrency("NGN");
        d.setScheduledAt(request.getScheduledAt());

        if (request.isAutoAssign()) {
            // Step 3: Nearest available rider in zone
            List<Rider> availableRiders = riderRepository.findByZone_IdAndAvailableTrueAndVerificationStatus(zone.getId(), Rider.VerificationStatus.APPROVED);
            if (availableRiders.isEmpty()) {
                throw new IllegalArgumentException("No available riders in zone");
            }
            Rider nearest = null;
            double minDistance = Double.MAX_VALUE;
            for (Rider rider : availableRiders) {
                if (rider.getCurrentLat() == null || rider.getCurrentLng() == null) continue;
                double dist = GeoUtils.distanceKm(rider.getCurrentLat(), rider.getCurrentLng(), storeLat, storeLng);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = rider;
                }
            }
            if (nearest == null) {
                throw new IllegalArgumentException("No riders in zone with location set");
            }
            d.setRider(nearest);
            d.setStatus(DeliveryOrder.DeliveryStatus.ASSIGNED);
            nearest.setAvailable(false);
        } else {
            d.setStatus(DeliveryOrder.DeliveryStatus.PENDING);
        }

        d = deliveryOrderRepository.save(d);

        DeliveryOrderResponse resp = DeliveryOrderResponse.from(d);
        resp.setOrderId(order.getPublicId());
        if (d.getRider() != null) {
            eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), "PENDING", "ASSIGNED", d.getRider().getPublicId()));
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryOrderResponse> listDeliveryOrdersByStore(Long storeId, String status, Pageable pageable) {
        Page<DeliveryOrder> page = status != null && !status.isBlank()
                ? deliveryOrderRepository.findByStoreIdAndStatus(storeId, DeliveryOrder.DeliveryStatus.valueOf(status), pageable)
                : deliveryOrderRepository.findByStoreId(storeId, pageable);
        Page<DeliveryOrderResponse> resp = page.map(d -> {
            DeliveryOrderResponse r = DeliveryOrderResponse.from(d);
            orderRepository.findById(d.getOrderId()).ifPresent(o -> r.setOrderId(o.getPublicId()));
            return r;
        });
        return PageResponse.of(resp);
    }

    @Transactional(readOnly = true)
    public DeliveryOrderResponse getDeliveryOrderByStore(Long storeId, String deliveryOrderPublicId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        if (!d.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId);
        }
        DeliveryOrderResponse r = DeliveryOrderResponse.from(d);
        orderRepository.findById(d.getOrderId()).ifPresent(o -> r.setOrderId(o.getPublicId()));
        return r;
    }

    @Transactional
    public DeliveryOrderResponse assignRider(Long storeId, String deliveryOrderPublicId, AssignRiderRequest request) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        if (!d.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId);
        }
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("Can only assign rider to PENDING delivery");
        }
        Rider rider = riderRepository.findByPublicId(request.getRiderPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Rider", request.getRiderPublicId()));
        if (!rider.isAvailable()) {
            throw new IllegalArgumentException("Rider is not available");
        }
        String previousStatus = d.getStatus().name();
        d.setRider(rider);
        d.setStatus(DeliveryOrder.DeliveryStatus.ASSIGNED);
        rider.setAvailable(false);
        d = deliveryOrderRepository.save(d);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, d.getStatus().name(), rider.getPublicId()));
        DeliveryOrderResponse r = DeliveryOrderResponse.from(d);
        orderRepository.findById(d.getOrderId()).ifPresent(o -> r.setOrderId(o.getPublicId()));
        return r;
    }

    @Transactional
    public DeliveryOrderResponse rescheduleDelivery(Long storeId, String deliveryOrderPublicId, com.shopper.domain.logistics.dto.RescheduleDeliveryRequest request) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(deliveryOrderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId));
        if (!d.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("DeliveryOrder", deliveryOrderPublicId);
        }
        if (d.getStatus() != DeliveryOrder.DeliveryStatus.FAILED && d.getStatus() != DeliveryOrder.DeliveryStatus.RETURNED) {
            throw new IllegalArgumentException("Can only reschedule FAILED or RETURNED delivery");
        }
        if (d.getRider() != null) {
            d.getRider().setAvailable(true);
        }
        String previousStatus = d.getStatus().name();
        d.setStatus(DeliveryOrder.DeliveryStatus.PENDING);
        d.setRider(null);
        d.setFailedReason(null);
        if (request.getScheduledAt() != null) d.setScheduledAt(request.getScheduledAt());
        d = deliveryOrderRepository.save(d);
        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(this, d.getPublicId(), previousStatus, d.getStatus().name(), null));
        DeliveryOrderResponse r = DeliveryOrderResponse.from(d);
        orderRepository.findById(d.getOrderId()).ifPresent(o -> r.setOrderId(o.getPublicId()));
        return r;
    }

    @Transactional(readOnly = true)
    public DeliveryOrder getDeliveryOrderEntity(String publicId) {
        return deliveryOrderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", publicId));
    }

    @Transactional(readOnly = true)
    public Rider getRiderEntity(String publicId) {
        return riderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", publicId));
    }

    // --- Riders (admin) ---
    @Transactional(readOnly = true)
    public PageResponse<RiderResponse> listRiders(String status, String zonePublicId, Pageable pageable) {
        Page<Rider> page;
        if (status != null && !status.isBlank()) {
            page = riderRepository.findByStatus(Rider.RiderStatus.valueOf(status), pageable);
        } else if (zonePublicId != null && !zonePublicId.isBlank()) {
            Long zoneId = serviceZoneRepository.findByPublicId(zonePublicId)
                    .orElseThrow(() -> new ResourceNotFoundException("ServiceZone", zonePublicId))
                    .getId();
            page = riderRepository.findByZone_Id(zoneId, pageable);
        } else {
            page = riderRepository.findAll(pageable);
        }
        return PageResponse.of(page.map(RiderResponse::from));
    }

    @Transactional(readOnly = true)
    public RiderResponse getRider(String publicId) {
        Rider rider = riderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", publicId));
        return RiderResponse.from(rider);
    }

    @Transactional
    public RiderResponse updateRider(String publicId, UpdateRiderRequest request) {
        Rider rider = riderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", publicId));
        if (request.getPhone() != null) rider.setPhone(request.getPhone());
        if (request.getEmail() != null) rider.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            rider.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) rider.setFirstName(request.getFirstName());
        if (request.getLastName() != null) rider.setLastName(request.getLastName());
        if (request.getStatus() != null) rider.setStatus(Rider.RiderStatus.valueOf(request.getStatus()));
        if (request.getZonePublicId() != null) {
            if (request.getZonePublicId().isBlank()) {
                rider.setZone(null);
            } else {
                rider.setZone(serviceZoneRepository.findByPublicId(request.getZonePublicId())
                        .orElseThrow(() -> new ResourceNotFoundException("ServiceZone", request.getZonePublicId())));
            }
        }
        rider = riderRepository.save(rider);
        return RiderResponse.from(rider);
    }

    @Transactional
    public RiderResponse updateRiderVerification(String riderPublicId, com.shopper.domain.logistics.dto.RiderVerificationRequest request) {
        Rider rider = riderRepository.findByPublicId(riderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider", riderPublicId));
        Rider.VerificationStatus status = Rider.VerificationStatus.valueOf(request.getStatus());
        if (status != Rider.VerificationStatus.APPROVED && status != Rider.VerificationStatus.REJECTED) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }
        if (status == Rider.VerificationStatus.REJECTED && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting");
        }
        rider.setVerificationStatus(status);
        rider.setRejectionReason(status == Rider.VerificationStatus.REJECTED ? request.getRejectionReason() : null);
        rider = riderRepository.save(rider);
        return RiderResponse.from(rider);
    }
}
