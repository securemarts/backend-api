package com.securemarts.domain.cart.controller;

import com.securemarts.domain.cart.dto.CreateOrderAndPayRequest;
import com.securemarts.domain.cart.dto.CreateOrderAndPayResponse;
import com.securemarts.domain.cart.service.CheckoutService;
import com.securemarts.domain.order.dto.OrderResponse;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storePublicId}/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Convert cart to order")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final StoreRepository storeRepository;

    @PostMapping("/create-order")
    @Operation(summary = "Create order from cart", description = "Converts cart to order and clears cart")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable String storePublicId,
            @RequestParam String cartId) {
        Long storeId = resolveStoreId(storePublicId);
        Long customerId = null;
        Order order = checkoutService.createOrderFromCart(storeId, customerId, cartId, null, null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/create-order-and-pay")
    @Operation(summary = "Create order and initiate payment", description = "Creates order from cart and initiates payment in one call. Payment is linked to the order (order_id saved). Return payment.authorizationUrl to redirect customer to gateway.")
    public ResponseEntity<CreateOrderAndPayResponse> createOrderAndPay(
            @PathVariable String storePublicId,
            @Valid @RequestBody CreateOrderAndPayRequest request) {
        Long storeId = resolveStoreId(storePublicId);
        CreateOrderAndPayResponse response = checkoutService.createOrderAndInitiatePayment(
                storeId,
                null,
                request.getCartId(),
                request.getEmail(),
                request.getCallbackUrl(),
                request.getGateway(),
                request.getDeliveryAddress(),
                request.getDeliveryLat(),
                request.getDeliveryLng());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
    }
}
