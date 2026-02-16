package com.securemarts.domain.order.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.order.dto.OrderResponse;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.order.repository.OrderRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long storeId, String orderPublicId) {
        Order order = orderRepository.findByPublicId(orderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderPublicId));
        if (!order.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Order", orderPublicId);
        }
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listOrders(Long storeId, String status, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
        Page<Order> page = status != null && !status.isBlank()
                ? orderRepository.findByStoreIdAndStatus(storeId, Order.OrderStatus.valueOf(status), pageable)
                : orderRepository.findByStoreId(storeId, pageable);
        return PageResponse.of(page.map(OrderResponse::from));
    }

    @Transactional
    public OrderResponse updateStatus(Long storeId, String orderPublicId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findByPublicId(orderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderPublicId));
        if (!order.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Order", orderPublicId);
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }
}
