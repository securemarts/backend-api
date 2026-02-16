package com.securemarts.domain.logistics.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeliveryStatusChangedEvent extends ApplicationEvent {

    private final String deliveryOrderPublicId;
    private final String previousStatus;
    private final String newStatus;
    private final String riderPublicId;

    public DeliveryStatusChangedEvent(Object source, String deliveryOrderPublicId, String previousStatus, String newStatus, String riderPublicId) {
        super(source);
        this.deliveryOrderPublicId = deliveryOrderPublicId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.riderPublicId = riderPublicId;
    }
}
