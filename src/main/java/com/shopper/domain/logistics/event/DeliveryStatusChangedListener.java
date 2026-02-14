package com.shopper.domain.logistics.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryStatusChangedListener {

    @EventListener
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        log.info("Delivery status changed: deliveryOrderPublicId={}, previousStatus={}, newStatus={}, riderPublicId={}",
                event.getDeliveryOrderPublicId(), event.getPreviousStatus(), event.getNewStatus(), event.getRiderPublicId());
        // External push service can subscribe to this event (e.g. call webhook or FCM)
    }
}
