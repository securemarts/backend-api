package com.securemarts.domain.logistics.partner;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.logistics.entity.DeliveryOrder;
import com.securemarts.domain.logistics.repository.DeliveryOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * In-house delivery: no external API. Deliveries are created via LogisticsService.createDeliveryOrder;
 * status updates come from rider app. This adapter supports getStatus/cancel by delivery publicId.
 */
@Component
@RequiredArgsConstructor
public class InternalLogisticsPartner implements LogisticsPartner {

    private static final String CARRIER_CODE = "INTERNAL";

    private final DeliveryOrderRepository deliveryOrderRepository;

    @Override
    public String getCarrierCode() {
        return CARRIER_CODE;
    }

    @Override
    public ShipmentResult createShipment(ShipmentRequest request) {
        return ShipmentResult.builder()
                .success(true)
                .message("Use LogisticsService.createDeliveryOrder for internal carrier")
                .build();
    }

    @Override
    public String getStatus(String externalShipmentId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(externalShipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", externalShipmentId));
        return d.getStatus().name();
    }

    @Override
    public void cancelShipment(String externalShipmentId) {
        DeliveryOrder d = deliveryOrderRepository.findByPublicId(externalShipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", externalShipmentId));
        d.setStatus(DeliveryOrder.DeliveryStatus.FAILED);
        d.setFailedReason("Cancelled");
        deliveryOrderRepository.save(d);
    }
}
