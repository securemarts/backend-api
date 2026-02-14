package com.shopper.domain.cart.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.cart.entity.Cart;
import com.shopper.domain.cart.entity.CartItem;
import com.shopper.domain.cart.repository.CartRepository;
import com.shopper.domain.inventory.service.InventoryService;
import com.shopper.domain.order.dto.OrderResponse;
import com.shopper.domain.order.entity.Order;
import com.shopper.domain.order.entity.OrderItem;
import com.shopper.domain.order.repository.OrderRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.payment.dto.InitiatePaymentRequest;
import com.shopper.domain.payment.dto.PaymentResponse;
import com.shopper.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    @Transactional
    public Order createOrderFromCart(Long storeId, Long customerId, String cartPublicId) {
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
        order = orderRepository.save(order);
        for (CartItem ci : cart.getItems()) {
            inventoryService.deductVariantQuantity(storeId, ci.getProductVariant().getPublicId(), ci.getQuantity(), "ORDER", order.getPublicId());
        }
        cart.getItems().clear();
        cartRepository.save(cart);
        return order;
    }

    /**
     * Creates order from cart and initiates payment in one call. Payment is linked to the order (order_id saved).
     */
    @Transactional
    public com.shopper.domain.cart.dto.CreateOrderAndPayResponse createOrderAndInitiatePayment(
            Long storeId,
            Long customerId,
            String cartPublicId,
            String email,
            String callbackUrl,
            String gateway) {
        Order order = createOrderFromCart(storeId, customerId, cartPublicId);
        InitiatePaymentRequest payRequest = new InitiatePaymentRequest();
        payRequest.setEmail(email);
        payRequest.setAmount(order.getTotalAmount());
        payRequest.setCurrency(order.getCurrency() != null ? order.getCurrency() : "NGN");
        payRequest.setOrderId(order.getPublicId());
        payRequest.setCallbackUrl(callbackUrl);
        payRequest.setGateway(gateway != null && !gateway.isBlank() ? gateway : "PAYSTACK");
        PaymentResponse paymentResponse = paymentService.initiate(storeId, payRequest);
        return com.shopper.domain.cart.dto.CreateOrderAndPayResponse.builder()
                .order(OrderResponse.from(order))
                .payment(paymentResponse)
                .build();
    }

    private String generateOrderNumber(Long storeId) {
        long count = orderRepository.countByStoreId(storeId);
        return "ORD-" + String.format("%08d", count + 1);
    }
}
