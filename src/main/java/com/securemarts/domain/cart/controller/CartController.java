package com.securemarts.domain.cart.controller;

import com.securemarts.domain.cart.dto.CartItemRequest;
import com.securemarts.domain.cart.dto.CartResponse;
import com.securemarts.domain.cart.service.CartService;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.StoreChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final StoreChannelService storeChannelService;

    @GetMapping
    @Operation(summary = "Get cart by token or ID", description = "Pass X-Cart-Token header or cartPublicId query")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable String storePublicId,
            @Parameter(description = "Cart public ID (alternative to X-Cart-Token header)", schema = @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")) @RequestParam(required = false) String cartId,
            @Parameter(description = "Cart token from previous cart response (X-Cart-Token header)") @RequestHeader(value = "X-Cart-Token", required = false) String cartToken) {
        Long storeId = resolveStoreAndEnsureOnline(storePublicId);
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
            @Parameter(description = "Cart token (X-Cart-Token header). Omit to create a new cart.") @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @Valid @RequestBody CartItemRequest request) {
        Long storeId = resolveStoreAndEnsureOnline(storePublicId);
        CartResponse cart = cartService.addItem(storeId, null, cartToken, request);
        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/{cartPublicId}/items/{cartItemPublicId}")
    @Operation(summary = "Update item quantity (0 to remove)")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable String storePublicId,
            @Parameter(description = "Cart public ID") @PathVariable String cartPublicId,
            @Parameter(description = "Cart item public ID") @PathVariable String cartItemPublicId,
            @RequestBody java.util.Map<String, Integer> body) {
        Long storeId = resolveStoreAndEnsureOnline(storePublicId);
        int qty = body != null && body.containsKey("quantity") ? body.get("quantity") : 0;
        return ResponseEntity.ok(cartService.updateItemQuantity(storeId, cartPublicId, cartItemPublicId, qty));
    }

    @DeleteMapping("/{cartPublicId}/items/{cartItemPublicId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String storePublicId,
            @Parameter(description = "Cart public ID") @PathVariable String cartPublicId,
            @Parameter(description = "Cart item public ID") @PathVariable String cartItemPublicId) {
        Long storeId = resolveStoreAndEnsureOnline(storePublicId);
        return ResponseEntity.ok(cartService.removeItem(storeId, cartPublicId, cartItemPublicId));
    }

    private Long resolveStoreAndEnsureOnline(String storePublicId) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", storePublicId));
        storeChannelService.ensureOnlineEnabled(store);
        return store.getId();
    }
}
