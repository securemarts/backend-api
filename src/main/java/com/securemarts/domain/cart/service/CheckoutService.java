package com.securemarts.domain.cart.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.cart.entity.Cart;
import com.securemarts.domain.cart.entity.CartItem;
import com.securemarts.domain.cart.repository.CartRepository;
import com.securemarts.domain.inventory.entity.InventoryItem;
import com.securemarts.domain.inventory.entity.Location;
import com.securemarts.domain.inventory.service.InventoryService;
import com.securemarts.domain.order.dto.OrderResponse;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.order.entity.OrderItem;
import com.securemarts.domain.order.entity.OrderItemAllocation;
import com.securemarts.domain.order.entity.Shipment;
import com.securemarts.domain.order.repository.OrderItemAllocationRepository;
import com.securemarts.domain.order.repository.OrderRepository;
import com.securemarts.domain.order.repository.ShipmentRepository;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.StoreChannelService;
import com.securemarts.domain.payment.dto.InitiatePaymentRequest;
import com.securemarts.domain.payment.dto.PaymentResponse;
import com.securemarts.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderItemAllocationRepository orderItemAllocationRepository;
    private final StoreRepository storeRepository;
    private final StoreChannelService storeChannelService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    @Transactional
    public Order createOrderFromCart(Long storeId, Long customerId, String cartPublicId, String deliveryAddress, java.math.BigDecimal deliveryLat, java.math.BigDecimal deliveryLng) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        storeChannelService.ensureOnlineEnabled(store);
        Cart cart = cartService.getCart(storeId, cartPublicId);
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }
        for (CartItem ci : cart.getItems()) {
            int available = inventoryService.getAvailableQuantityForVariant(storeId, ci.getProductVariant().getPublicId());
            if (ci.getQuantity() > available) {
                throw new BusinessRuleException(
                    "Insufficient stock for " + ci.getProductVariant().getTitle() + ". Available: " + available + ", requested: " + ci.getQuantity());
            }
        }
        String orderNumber = generateOrderNumber(storeId);
        Order order = new Order();
        order.setStoreId(storeId);
        order.setCustomerId(customerId != null ? customerId : cart.getCustomerId());
        order.setOrderNumber(orderNumber);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrigin(Order.OrderOrigin.ONLINE);
        order.setCurrency(storeRepository.findById(storeId).map(s -> s.getDefaultCurrency()).orElse("NGN"));
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductVariant(ci.getProductVariant());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getProductVariant().getPriceAmount());
            oi.setTotalPrice(ci.getProductVariant().getPriceAmount().multiply(BigDecimal.valueOf(ci.getQuantity())));
            order.getItems().add(oi);
            total = total.add(oi.getTotalPrice());
        }
        order.setTotalAmount(total);
        if (deliveryAddress != null && !deliveryAddress.isBlank()) {
            order.setDeliveryAddress(deliveryAddress);
            if (deliveryLat != null) order.setDeliveryLat(deliveryLat);
            if (deliveryLng != null) order.setDeliveryLng(deliveryLng);
        }
        order.setReservationExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        order = orderRepository.save(order);
        // Allocate each line to location(s) and create shipments, then reserve per allocation
        List<OrderItem> sortedItems = order.getItems().stream()
                .sorted(Comparator.comparing(oi -> oi.getProductVariant().getId()))
                .toList();
        List<OrderItemAllocation> allocations = new ArrayList<>();
        Set<Long> locationIds = new HashSet<>();
        for (OrderItem oi : sortedItems) {
            List<InventoryItem> candidates = inventoryService.getAllocationCandidatesForVariant(storeId, oi.getProductVariant().getPublicId());
            if (candidates.isEmpty()) {
                throw new BusinessRuleException("No inventory for variant " + oi.getProductVariant().getTitle());
            }
            int remaining = oi.getQuantity();
            for (InventoryItem item : candidates) {
                if (remaining <= 0) break;
                int take = Math.min(remaining, item.getQuantityAvailable());
                if (take <= 0) continue;
                OrderItemAllocation alloc = new OrderItemAllocation();
                alloc.setOrderItem(oi);
                alloc.setLocation(item.getLocation());
                alloc.setQuantity(take);
                allocations.add(alloc);
                locationIds.add(item.getLocation().getId());
                remaining -= take;
            }
            if (remaining > 0) {
                throw new BusinessRuleException("Insufficient stock for " + oi.getProductVariant().getTitle() + ". Requested: " + oi.getQuantity());
            }
        }
        Map<Long, Location> locationMap = new HashMap<>();
        for (OrderItemAllocation a : allocations) {
            locationMap.put(a.getLocation().getId(), a.getLocation());
        }
        for (Long locId : locationIds) {
            Shipment sh = new Shipment();
            sh.setOrder(order);
            sh.setLocation(locationMap.get(locId));
            sh.setStatus(Shipment.ShipmentStatus.PENDING);
            shipmentRepository.save(sh);
        }
        orderItemAllocationRepository.saveAll(allocations);
        for (OrderItemAllocation a : allocations) {
            var invItem = inventoryService.getOrCreateInventoryItem(storeId, a.getOrderItem().getProductVariant().getPublicId(), a.getLocation().getPublicId());
            inventoryService.reserve(storeId, invItem.getPublicId(), a.getQuantity(), "ORDER", order.getPublicId());
        }
        cart.getItems().clear();
        cartRepository.save(cart);
        return order;
    }

    /**
     * Creates order from cart and initiates payment in one call. Payment is linked to the order (order_id saved).
     * If deliveryAddress and deliveryLat/deliveryLng are provided, a delivery order is created when payment succeeds.
     */
    @Transactional
    public com.securemarts.domain.cart.dto.CreateOrderAndPayResponse createOrderAndInitiatePayment(
            Long storeId,
            Long customerId,
            String cartPublicId,
            String email,
            String callbackUrl,
            String gateway,
            String deliveryAddress,
            java.math.BigDecimal deliveryLat,
            java.math.BigDecimal deliveryLng) {
        Order order = createOrderFromCart(storeId, customerId, cartPublicId, deliveryAddress, deliveryLat, deliveryLng);
        InitiatePaymentRequest payRequest = new InitiatePaymentRequest();
        payRequest.setEmail(email);
        payRequest.setAmount(order.getTotalAmount());
        payRequest.setCurrency(order.getCurrency() != null ? order.getCurrency() : "NGN");
        payRequest.setOrderId(order.getPublicId());
        payRequest.setCallbackUrl(callbackUrl);
        payRequest.setGateway(gateway != null && !gateway.isBlank() ? gateway : "PAYSTACK");
        PaymentResponse paymentResponse = paymentService.initiate(storeId, payRequest);
        return com.securemarts.domain.cart.dto.CreateOrderAndPayResponse.builder()
                .order(OrderResponse.from(order))
                .payment(paymentResponse)
                .build();
    }

    /**
     * Cancel checkout: release reservation and set order to CANCELLED. Only for PENDING orders with reservation.
     */
    @Transactional
    public void cancelCheckout(Long storeId, String orderPublicId) {
        Order order = orderRepository.findByPublicId(orderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderPublicId));
        if (!order.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Order", orderPublicId);
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING orders can be cancelled at checkout");
        }
        inventoryService.releaseByReference(storeId, "ORDER", order.getPublicId());
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generateOrderNumber(Long storeId) {
        long count = orderRepository.countByStoreId(storeId);
        return "ORD-" + String.format("%08d", count + 1);
    }
}
