package com.shopper.domain.cart.controller;

import com.shopper.domain.cart.dto.CartItemRequest;
import com.shopper.domain.cart.dto.CartResponse;
import com.shopper.domain.cart.service.CartService;
import com.shopper.domain.onboarding.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storePublicId}/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Create cart, add/remove items, get cart (storefront)")
public class CartController {

    private final CartService cartService;
    private final StoreRepository storeRepository;

    @GetMapping
    @Operation(summary = "Get cart by token or ID", description = "Pass X-Cart-Token header or cartPublicId query")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable String storePublicId,
            @RequestParam(required = false) String cartId,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken) {
        Long storeId = resolveStoreId(storePublicId);
        Long customerId = null;
        if (cartId != null && !cartId.isBlank()) {
            return ResponseEntity.ok(cartService.getCartResponse(storeId, cartId));
        }
        return ResponseEntity.ok(cartService.getOrCreateCartResponse(storeId, customerId, cartToken));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Creates cart if needed. Return cart includes X-Cart-Token.")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable String storePublicId,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @Valid @RequestBody CartItemRequest request) {
        Long storeId = resolveStoreId(storePublicId);
        CartResponse cart = cartService.addItem(storeId, null, cartToken, request);
        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/{cartPublicId}/items/{cartItemPublicId}")
    @Operation(summary = "Update item quantity (0 to remove)")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable String storePublicId,
            @PathVariable String cartPublicId,
            @PathVariable String cartItemPublicId,
            @RequestBody java.util.Map<String, Integer> body) {
        Long storeId = resolveStoreId(storePublicId);
        int qty = body != null && body.containsKey("quantity") ? body.get("quantity") : 0;
        return ResponseEntity.ok(cartService.updateItemQuantity(storeId, cartPublicId, cartItemPublicId, qty));
    }

    @DeleteMapping("/{cartPublicId}/items/{cartItemPublicId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String storePublicId,
            @PathVariable String cartPublicId,
            @PathVariable String cartItemPublicId) {
        Long storeId = resolveStoreId(storePublicId);
        return ResponseEntity.ok(cartService.removeItem(storeId, cartPublicId, cartItemPublicId));
    }

    private Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.shopper.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new com.shopper.common.exception.ResourceNotFoundException("Store", storePublicId));
    }
}
