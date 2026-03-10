package com.securemarts.domain.order.scheduler;

import com.securemarts.domain.inventory.service.InventoryService;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Releases inventory reserved for checkout when the reservation window has expired (e.g. 30 min).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryJob {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Scheduled(fixedDelayString = "${app.reservation-expiry-job-interval-ms:300000}") // default 5 min
    @Transactional
    public void releaseExpiredReservations() {
        List<Order> expired = orderRepository.findByStatusAndReservationExpiresAtBefore(Order.OrderStatus.PENDING, Instant.now());
        for (Order order : expired) {
            if (order.getReservationExpiresAt() == null) continue;
            try {
                inventoryService.releaseByReference(order.getStoreId(), "ORDER", order.getPublicId());
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                log.debug("Released expired reservation for order {}", order.getPublicId());
            } catch (Exception e) {
                log.warn("Failed to release expired reservation for order {}: {}", order.getPublicId(), e.getMessage());
            }
        }
    }
}
