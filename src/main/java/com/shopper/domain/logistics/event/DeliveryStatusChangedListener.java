package com.shopper.domain.logistics.event;

import com.shopper.domain.logistics.repository.DeliveryOrderRepository;
import com.shopper.domain.logistics.repository.RiderRepository;
import com.shopper.domain.onboarding.repository.StoreProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.shopper.domain.rider.sse.RiderSseRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryStatusChangedListener {

    private final RiderSseRegistry riderSseRegistry;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final StoreProfileRepository storeProfileRepository;
    private final RiderRepository riderRepository;

    @EventListener
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        log.info("Delivery status changed: deliveryOrderPublicId={}, previousStatus={}, newStatus={}, riderPublicId={}",
                event.getDeliveryOrderPublicId(), event.getPreviousStatus(), event.getNewStatus(), event.getRiderPublicId());

        if (event.getRiderPublicId() != null && !event.getRiderPublicId().isBlank()) {
            Map<String, Object> payload = Map.of(
                    "type", "delivery_assigned",
                    "deliveryOrderPublicId", event.getDeliveryOrderPublicId(),
                    "previousStatus", event.getPreviousStatus() != null ? event.getPreviousStatus() : "",
                    "newStatus", event.getNewStatus()
            );
            riderSseRegistry.sendToRider(event.getRiderPublicId(), payload);
        }

        if ("PENDING".equals(event.getNewStatus()) && (event.getRiderPublicId() == null || event.getRiderPublicId().isBlank())) {
            deliveryOrderRepository.findByPublicId(event.getDeliveryOrderPublicId()).ifPresent(d -> {
                storeProfileRepository.findByStoreIdWithZone(d.getStoreId()).filter(p -> p.getZone() != null).ifPresent(profile -> {
                    List<String> riderPublicIds = riderRepository.findByZone_Id(profile.getZone().getId(), Pageable.unpaged())
                            .getContent().stream()
                            .map(r -> r.getPublicId())
                            .collect(Collectors.toList());
                    if (!riderPublicIds.isEmpty()) {
                        Map<String, Object> payload = Map.of(
                                "type", "delivery_available",
                                "deliveryOrderPublicId", event.getDeliveryOrderPublicId()
                        );
                        riderSseRegistry.sendToRiders(riderPublicIds, payload);
                    }
                });
            });
        }
    }
}
